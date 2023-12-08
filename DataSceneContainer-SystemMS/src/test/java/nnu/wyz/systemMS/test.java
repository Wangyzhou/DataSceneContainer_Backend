package nnu.wyz.systemMS;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.ReturnUsersByEmailLikeDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscSceneService;
import nnu.wyz.systemMS.utils.CompareUtil;
import nnu.wyz.systemMS.websocket.WebSocketServer;
import nnu.wyz.systemMS.service.DscGDVSceneService;
import nnu.wyz.systemMS.service.DscGeoJSONService;
import nnu.wyz.systemMS.utils.GeoJSONUtil;
import nnu.wyz.systemMS.utils.MimeTypesUtil;
import nnu.wyz.systemMS.utils.RedisCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 17:00
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class test {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;
    @Autowired
    private DscMessageDAO dscMessageDAO;

    @Autowired
    private DscFileDAO dscFileDAO;
    @Autowired
    private DscGDVSceneService dscGDVSceneService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void test1() {
        String ext = MimeTypesUtil.getDefaultExt("application/zip");
        System.out.println("ext = " + ext);
    }

    @Test
    void testCopyFile() {
        CopyObjectRequest copyObjectRequest = new CopyObjectRequest(minioConfig.getBucketName(), "2023-08-31/ebd89c91-41d9-472b-9648-bcc217499a60.pdf", minioConfig.getBucketName(), "2023-08-31/www.pdf");
        CopyObjectResult copyObjectResult = amazonS3.copyObject(copyObjectRequest);
        System.out.println("copyObjectResult = " + copyObjectResult);
    }

    @Test
    void testGetFile() {
        GetObjectRequest getObjectRequest = new GetObjectRequest(minioConfig.getBucketName(), "2023-08-31/ebd89c91-41d9-472b-9648-bcc217499a60.pdf");
        S3Object object = amazonS3.getObject(getObjectRequest);
        ObjectMetadata objectMetadata = object.getObjectMetadata();
        String eTag = objectMetadata.getETag();
        System.out.println("eTag = " + eTag);
    }

    @Test
    void testUpload() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream("D:/12121212.png");
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("img/png");
        PutObjectRequest putObjectRequest = new PutObjectRequest(minioConfig.getBucketName(), "2023-09-15/hahahar.png", fileInputStream, objectMetadata);
        PutObjectResult putObjectResult = amazonS3.putObject(putObjectRequest);
        System.out.println("putObjectResult = " + putObjectResult);
    }

    @Test
    void testCatalogTree() {
        String catalogId = "1bd88385-4252-42f2-ab19-751a6b294da8";
        String userId = "64eda0524debf898422c7919";
        List<JSONObject> precursion = precursion(catalogId);
        System.out.println("precursion = " + precursion);
    }

    List<JSONObject> precursion(String catalogId) {
        Optional<DscCatalog> byId = dscCatalogDAO.findById(catalogId);
        DscCatalog dscCatalog = byId.get();
        if (dscCatalog.getChildren().size() == 0) {
            return null;
        }
        ArrayList<JSONObject> catalogItems = new ArrayList<>();
        for (CatalogChildrenDTO childrenDTO :
                dscCatalog.getChildren()) {
            if (childrenDTO.getType().equals("folder")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", childrenDTO.getId());
                jsonObject.put("label", childrenDTO.getName());
                List<JSONObject> precursion = precursion(childrenDTO.getId());
                if (precursion != null) {
                    jsonObject.put("children", precursion);
                }
                catalogItems.add(jsonObject);
            }
        }
        return catalogItems;
    }

    @Test
    void testStaticFile() {
        try {
            String path = ResourceUtils.getURL("classpath:").getPath() + "\\static\\scene-thumbnails";
            System.out.println("path = " + path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testtt() {
        String url = "http://172.21.213.86:9000/scene-thumbnails/64eda0524debf898422c7919/fadaeb18-bf82-45c4-9137-5477a2bd79ef.png";
        String userId = "64eda0524debf898422c7919";
        int oKBegin = url.indexOf(userId);
        String objectKey = url.substring(oKBegin);
        System.out.println("objectKey = " + objectKey);
    }

    @Test
    void test222() {
        ArrayList<Integer> integers = new ArrayList<>();
        Integer integer = Integer.valueOf(1);
        integers.add(integer);
        integers.add(integer);
        integers.add(integer);
        integers.remove(integer);
        System.out.println("integers = " + integers);
    }

    @Test
    void readCpgFile() throws IOException {
        String filePath = "C:\\Users\\Administrator\\Desktop\\gdata\\gdata\\js_river.cpg";
        FileInputStream fileInputStream = new FileInputStream(filePath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
        String code = bufferedReader.readLine();
        System.out.println("code = " + code);
    }

    @Test
    void testBeanUtil() {
        DscGDVSceneConfig gdvSceneConfig = dscGDVSceneService.getGDVSceneConfig("f9ef4a6e-7796-45a4-b9cb-d795e9eff0d6");
        JSONObject sceneConfig = BeanUtil.toBean(gdvSceneConfig, JSONObject.class);
        System.out.println("sceneConfig = " + sceneConfig);
    }

    @Test
    void testRedis() {
        List<Object> wqejbnjqhwnber = redisCache.getCacheList("wqejbnjqhwnber");
        System.out.println("wqejbnjqhwnber = " + wqejbnjqhwnber);
    }

    @Test
    void testMessageCRUD() {
        Criteria criteria = new Criteria();
        Query query = new Query(criteria.orOperator(Criteria.where("to").is("64eda0524debf898422c7919"),
                Criteria.where("type").is("system")));
        query.with(Sort.by(Sort.Order.desc("date")));
        List<Message> allMsg = mongoTemplate.find(query, Message.class, "message");
        System.out.println("allMsg = " + allMsg);
    }

    @Autowired
    private WebSocketServer webSocketServer;

    @Test
    void testWebSocket() {
        Message message = new Message();
        message.setFrom("haha");
        message.setTo("wyz");
        message.setTopic("test");
        message.setType("test");
        message.setText("hahaha");
        webSocketServer.sendInfo("wyz", JSON.toJSONString(message));
    }

    @Test
    void testLikeQuery() {
        Criteria criteria = new Criteria();
        Pattern pattern = Pattern.compile("^" + "23" + ".*$");
        Query query = Query.query(criteria.andOperator(Criteria.where("email").regex(pattern), Criteria.where("enabled").is(0)));
        List<DscUser> dscUser = mongoTemplate.find(query, DscUser.class, "dscUser");
        ArrayList<ReturnUsersByEmailLikeDTO> returnUsers = new ArrayList<>();
        dscUser.forEach(dscUser1 -> {
            ReturnUsersByEmailLikeDTO returnUser = BeanUtil.copyProperties(dscUser1, ReturnUsersByEmailLikeDTO.class);
            returnUsers.add(returnUser);
        });
        System.out.println("returnUsers = " + returnUsers);
        System.out.println("dscUser = " + dscUser);
    }

    @Autowired
    private SysUploadTaskDAO sysUploadTaskDAO;

    @Test
    void testDeleteObjects() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
        List<DscFileInfo> allByOwnerCount = dscFileDAO.findAllByOwnerCount(0L);
        dscFileDAO.deleteAll(allByOwnerCount);
        List<SysUploadTask> collect = allByOwnerCount.stream().map(dscFileInfo -> sysUploadTaskDAO.findSysUploadTaskByFileId(dscFileInfo.getId())).filter(Objects::nonNull).collect(Collectors.toList());
        sysUploadTaskDAO.deleteAll(collect);
        List<DeleteObject> objects = allByOwnerCount.stream().map(dscFileInfo -> new DeleteObject(dscFileInfo.getObjectKey())).collect(Collectors.toList());
        Iterable<Result<DeleteError>> results =
                minioClient.removeObjects(
                        RemoveObjectsArgs.builder().bucket(minioConfig.getBucketName()).objects(objects).build());
        for (Result<DeleteError> result : results) {
            DeleteError error = result.get();
            System.out.println(
                    "Error in deleting object " + error.objectName() + "; " + error.message());
        }
        System.out.println("objects = " + objects);
    }

    @Test
    void test1431() {
        List<SysUploadTask> sysUploadTaskList = sysUploadTaskDAO.findAllByUploader("64f989e54deb825d3258d99f");
        sysUploadTaskDAO.deleteAll(sysUploadTaskList);
    }

    @Test
    void test14312() {
        List<DscFileInfo> dscFileInfoList = dscFileDAO.findAllByCreatedUser("64f989e54deb825d3258d99f");
        dscFileDAO.deleteAll(dscFileInfoList);
    }

    @Test
    void testParseGeoJSON() throws IOException {
//        jsonParser.parseGeoJSON();
        String fullPath = "D:\\global_earthquake.geojson";
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> rootJSON = objectMapper.readValue(new File(fullPath), Map.class);
        if (!rootJSON.containsKey("features") || !rootJSON.containsKey("type") || !"FeatureCollection".equals(rootJSON.get("type"))) {
            System.out.println("不支持的geojson结构");
        }
        ArrayList<Map<String, Object>> features = (ArrayList<Map<String, Object>>) rootJSON.get("features");
        int featureCount = features.size();
        System.out.println("featureCount = " + featureCount);
        Map<String, Object> geometry = (Map<String, Object>) features.get(0).get("geometry");
        System.out.println("geometry = " + geometry);
        String type = (String) geometry.get("type");
        System.out.println("type = " + type);
        List<Map<String, Object>> properties = features.stream().map(feature -> (Map<String, Object>) feature.get("properties")).collect(Collectors.toList());
        System.out.println("properties = " + properties);
        List<List<Object>> coordinatesList = features.stream().map(feature -> (List<Object>) (((Map<String, Object>) feature.get("geometry")).get("coordinates"))).collect(Collectors.toList());


    }

    List<Double> getBBOX(List<List<Double>> arr) {
        double minLng = 0.0, maxLng = 0.0, minLat = 0.0, maxLat = 0.0;
        for (List<Double> lngLat : arr) {
            double lng = lngLat.get(0);
            double lat = lngLat.get(1);
            minLng = Math.min(minLng, lng);
            maxLng = Math.max(maxLng, lng);
            minLat = Math.min(minLat, lat);
            maxLat = Math.max(maxLat, lat);
        }
        ArrayList<Double> bbox = new ArrayList<>();
        bbox.add(minLng);
        bbox.add(minLat);
        bbox.add(maxLng);
        bbox.add(maxLat);
        return bbox;
    }

    List<Double> getBBOXFromCoordinates(List<List<Object>> coordinatesList, String type) {
        double minLng = 0.0, maxLng = 0.0, minLat = 0.0, maxLat = 0.0;
        switch (type) {
            case "Point":

                break;
            case "MultiPoint":

                break;
            case "LineString":

                break;
            case "MultiLineString":

                break;
            case "Polygon":

                break;
            case "MultiPolygon":
                break;
        }

        return null;
    }

    @Test
    void testGeoJSONUtil() {
        GeoJSONUtil.initUtil("C:\\Users\\Administrator\\Downloads\\js_city_region_u.geojson");
        String geoJSONType = GeoJSONUtil.getGeoJSONType();
        System.out.println("geoJSONType = " + geoJSONType);
        List<Double> geoJSONBBOX = GeoJSONUtil.getGeoJSONBBOX();
        System.out.println("bbox = " + geoJSONBBOX);
    }

    @Autowired
    private DscGeoJSONService dscGeoJSONService;

    @Test
    void testGetGeoJSON() {
        GeoJSONUtil.initUtil("C:\\Users\\Administrator\\Desktop\\gdata\\ChinaProvince.geojson");
        String geoJSONType = GeoJSONUtil.getGeoJSONType();
        System.out.println("geoJSONType = " + geoJSONType);
        List<Double> geoJSONBBOX = GeoJSONUtil.getGeoJSONBBOX();
        System.out.println("bbox = " + geoJSONBBOX);
        List uniqueValues = GeoJSONUtil.getUniqueValues("Code", "asc");
        uniqueValues.forEach(System.out::println);
        List<String> fields = GeoJSONUtil.getFields();
        fields.forEach(System.out::println);
        int featureCount = GeoJSONUtil.getFeatureCount();
        System.out.println("featureCount = " + featureCount);
        List<Map<String, Object>> attrs = GeoJSONUtil.getAttrs();
        attrs.forEach(System.out::println);
    }

    @Test
    void testInitMinio() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException, BucketPolicyTooLargeException {
        String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::test\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\"],\"Resource\":[\"arn:aws:s3:::test/*\"]}]}";
        MinioClient minioClient = MinioClient.builder()
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .endpoint(minioConfig.getEndpoint())
                .build();
        String bucketPolicy = minioClient.getBucketPolicy(GetBucketPolicyArgs.builder().bucket("dsc-file").build());
        System.out.println("bucketPolicy = " + bucketPolicy);
        minioClient.makeBucket(MakeBucketArgs.builder().bucket("test").build());
        minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket("test").config(policy).build());
    }

    @Test
    void testTransfer() {
        GetObjectRequest getObjectRequest = new GetObjectRequest(minioConfig.getBucketName(), "652a5e61e4b012905c858bea/45b011a5-f04f-4c42-8fc3-12ae405e2d9d.shp");
        S3Object object = amazonS3.getObject(getObjectRequest);
        S3ObjectInputStream objectContent = object.getObjectContent();
        ObjectMetadata objectMetadata = object.getObjectMetadata();
        System.out.println("object = " + object);
    }

    @Autowired
    private PasswordEncoder bcryptPasswordEncoder;
    @Autowired
    private DscCatalogService dscCatalogService;
    @Autowired
    private DscUserDAO dscUserDAO;

    @Test
    void testRegister() {
        DscUser dscUser = new DscUser();
        dscUser.setId(IdUtil.objectId());
        dscUser.setEmail("admin4");
        dscUser.setPassword(bcryptPasswordEncoder.encode("123"));
        dscUser.setUserName("admin");
        dscUser.setInstitution("nnu");
        dscUser.setRegisterDate(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscUser.setEnabled(1);
        String activeCode = RandomUtil.randomString(5);
        dscUser.setActiveCode(activeCode);
        dscUserDAO.insert(dscUser);
        dscCatalogService.createRootCatalog(dscUser.getId());
    }

    @Test
    void test2122() {
        CommonResult<List<JSONObject>> catalogChildrenTree = dscCatalogService.getCatalogChildrenTree("ce344b9e-0b68-46b1-9765-e50922855b6f");
        System.out.println("catalogChildrenTree = " + catalogChildrenTree.getData());
    }

    @Test
    void testSortCatalog() {
        CommonResult<List<CatalogChildrenDTO>> children = dscCatalogService.getChildren("ce344b9e-0b68-46b1-9765-e50922855b6f", "652a48fde4b01213a180bb5a");
        List<CatalogChildrenDTO> data = children.getData();
        List<String> dataNoFolder = data.stream().filter(d -> !d.getType().equals("folder")).map(CatalogChildrenDTO::getName).collect(Collectors.toList());
        Collections.sort(dataNoFolder, (str1, str2) -> {
            // 处理数据为null的情况
            if (str1 == null && str2 == null) {
                return 0;
            }
            if (str1 == null) {
                return -1;
            }
            if (str2 == null) {
                return 1;
            }
            // 比较字符串中的每个字符
            char c1;
            char c2;
            // 逐字比较返回结果
            for (int i = 0; i < str1.length(); i++) {
                c1 = str1.charAt(i);
                try {
                    c2 = str2.charAt(i);
                } catch (StringIndexOutOfBoundsException e) { // 如果在该字符前，两个串都一样，str2更短，则str1较大
                    return 1;
                }
                // 如果都是数字的话，则需要考虑多位数的情况，取出完整的数字字符串，转化为数字再进行比较
                if (Character.isDigit(c1) && Character.isDigit(c2)) {
                    String numStr1 = "";
                    String numStr2 = "";
                    // 获取数字部分字符串
                    for (int j = i; j < str1.length(); j++) {
                        c1 = str1.charAt(j);
                        if (!Character.isDigit(c1) && c1 != '.') { // 不是数字则直接退出循环
                            break;
                        }
                        numStr1 += c1;
                    }
                    for (int j = i; j < str2.length(); j++) {
                        c2 = str2.charAt(j);
                        if (!Character.isDigit(c2) && c2 != '.') { // 考虑可能带小数的情况
                            break;
                        }
                        numStr2 += c2;
                    }
                    // 转换成数字数组进行比较 适配 1.25.3.5 这种情况
                    String[] numberArray1 = numberStrToNumberArray(numStr1);
                    String[] numberArray2 = numberStrToNumberArray(numStr2);
                    return compareNumberArray(numberArray1, numberArray2);
                }

                // 不是数字的比较方式
                if (c1 != c2) {
                    return c1 - c2;
                }
            }
            return 0;
        });
        dataNoFolder.add(15, "一寸照2.png");
        System.out.println("dataNoFolder = " + dataNoFolder);
        int index = CompareUtil.binarySearch(dataNoFolder, "一寸照2.png", 0, dataNoFolder.size() - 1);
//        System.out.println("index = " + index);
    }

    int binarySearch(List<String> arr, String target, int left, int right) {
        int mid = (left + right) / 2;
        if (left >= right) {
            return mid;
        }
        int compare = compare(arr.get(mid), target);
        if (compare >= 0) {
            return binarySearch(arr, target, left, mid);
        } else {
            return binarySearch(arr, target, mid + 1, right);
        }
    }

    public int compare(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return 0;
        }
        if (str1 == null) {
            return -1;
        }
        if (str2 == null) {
            return 1;
        }
        // 比较字符串中的每个字符
        char c1;
        char c2;
        // 逐字比较返回结果
        for (int i = 0; i < str1.length(); i++) {
            c1 = str1.charAt(i);
            try {
                c2 = str2.charAt(i);
            } catch (StringIndexOutOfBoundsException e) { // 如果在该字符前，两个串都一样，str2更短，则str1较大
                return 1;
            }
            // 如果都是数字的话，则需要考虑多位数的情况，取出完整的数字字符串，转化为数字再进行比较
            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                String numStr1 = "";
                String numStr2 = "";
                // 获取数字部分字符串
                for (int j = i; j < str1.length(); j++) {
                    c1 = str1.charAt(j);
                    if (!Character.isDigit(c1) && c1 != '.') { // 不是数字则直接退出循环
                        break;
                    }
                    numStr1 += c1;
                }
                for (int j = i; j < str2.length(); j++) {
                    c2 = str2.charAt(j);
                    if (!Character.isDigit(c2) && c2 != '.') { // 考虑可能带小数的情况
                        break;
                    }
                    numStr2 += c2;
                }
                // 转换成数字数组进行比较 适配 1.25.3.5 这种情况
                String[] numberArray1 = numberStrToNumberArray(numStr1);
                String[] numberArray2 = numberStrToNumberArray(numStr2);
                return compareNumberArray(numberArray1, numberArray2);
            }

            // 不是数字的比较方式
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return 0;
    }

    /**
     * 数字字符串转数字数组
     * 适配 1.25.3.5 这种情况 ，同时如果不不包含小数点【整数情况】
     *
     * @return
     */
    public static String[] numberStrToNumberArray(String numberStr) {
        // 按小数点分割字符串数组
        String[] numberArray = numberStr.split("\\.");
        // 长度为0说明没有小数点，则整个字符串作为第一个元素
        if (numberArray.length == 0) {
            numberArray = new String[]{numberStr};
        }
        return numberArray;

    }

    /**
     * 比较两个数字数组
     *
     * @param numberArray1
     * @param numberArray2
     * @return
     */
    public static int compareNumberArray(String[] numberArray1, String[] numberArray2) {
        for (int i = 0; i < numberArray1.length; i++) {
            if (numberArray2.length < i + 1) { // 此时数字数组2比1短，直接返回
                return 1;
            }
            int compareResult = Integer.valueOf(numberArray1[i]).compareTo(Integer.valueOf(numberArray2[i]));
            if (compareResult != 0) {
                return compareResult;
            }
        }
        // 说明数组1比数组2短，返回小于
        return -1;
    }

    @Test
    void testGetCatalogChildren() {
        PageableDTO pageableDTO = new PageableDTO();
        pageableDTO.setCriteria("ce344b9e-0b68-46b1-9765-e50922855b6f");
        pageableDTO.setPageIndex(2);
        pageableDTO.setPageSize(10);
        CommonResult<PageInfo<CatalogChildrenDTO>> childrenByPageable = dscCatalogService.getChildrenByPageable(pageableDTO);
        PageInfo<CatalogChildrenDTO> data = childrenByPageable.getData();
        System.out.println("data = " + data);
    }

    @Autowired
    private DscUserSceneDAO dscUserSceneDAO;
    @Autowired
    private DscSceneDAO dscSceneDAO;

    @Autowired
    private DscSceneService dscSceneService;

    @Test
    void testGetSceneListByTime() {
        int pageIndex = 1;
        int pageSize = 6;
        PageableDTO pageableDTO = new PageableDTO("652a5e61e4b012905c858bea", pageIndex, pageSize);
        CommonResult<PageInfo<DscScene>> sceneList = dscSceneService.getSceneList(pageableDTO);
        System.out.println("sceneList = " + sceneList);
        System.out.println(sceneList.getData());
//        List<DscScene> collect = dscUserSceneDAO.findAllByUserId("652a5e61e4b012905c858bea")
//                .stream()
//                .map(DscUserScene::getSceneId)
//                .map(dscSceneDAO::findById)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .sorted(Comparator.comparing(DscScene::getUpdatedTime).reversed())
//                .skip((long) (pageIndex - 1) * pageSize)
//                .limit(pageSize)
//                .collect(Collectors.toList());
//        System.out.println("collect = " + collect);
    }

    @Test
    void testGetSceneList() {
        List<DscUserScene> allByUserId = dscUserSceneDAO.findAllByUserId("652a48fde4b01213a180bb5a");

        allByUserId.stream()
                .map(DscUserScene::getSceneId)
                .map(dscSceneDAO::findById)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
                .forEach(System.out::println);
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .sorted(Comparator.comparing(DscScene::getUpdatedTime).reversed())
//                .collect(Collectors.toList());
//        System.out.println("collect = " + collect);
    }
    @Test

    void deleteFile() {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
        List<DscFileInfo> allByOwnerCount = dscFileDAO.findAllByOwnerCount(0L);
        dscFileDAO.deleteAll(allByOwnerCount);
        List<SysUploadTask> collect = allByOwnerCount.stream().map(dscFileInfo -> sysUploadTaskDAO.findSysUploadTaskByFileId(dscFileInfo.getId())).filter(Objects::nonNull).collect(Collectors.toList());
        sysUploadTaskDAO.deleteAll(collect);
        List<DeleteObject> objects = collect.stream().map(sysUploadTask -> new DeleteObject(sysUploadTask.getObjectKey())).collect(Collectors.toList());
        Iterable<Result<DeleteError>> results =
                minioClient.removeObjects(
                        RemoveObjectsArgs.builder().bucket(minioConfig.getBucketName()).objects(objects).build());
        for (Result<DeleteError> result : results) {
            DeleteError error = null;
            try {
                error = result.get();
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                e.printStackTrace();
            }
            System.out.println(
                    "Error in deleting object " + error.objectName() + "; " + error.message());
        }
        System.out.println("objects = " + objects);
        log.info("删除" + objects.size() + "个文件!");
        log.info("************定时任务执行结束************");
    }

    @Autowired
    private DscGeoToolsDAO dscGeoToolsDAO;
    @Test
    void testGeoTools() {
        List<DscGeoTools> all = dscGeoToolsDAO.findAll();
        System.out.println("all = " + all.get(0));
        ArrayList<JSONObject> Data_Tools = new ArrayList<>();
        ArrayList<JSONObject> GeomorphometricAnalysis = new ArrayList<>();
        ArrayList<JSONObject> GISAnalysis = new ArrayList<>();
        ArrayList<JSONObject> HydrologicalAnalysis = new ArrayList<>();
        ArrayList<JSONObject> ImageAnalysis = new ArrayList<>();
        ArrayList<JSONObject> LiDARAnalysis = new ArrayList<>();
        ArrayList<JSONObject> MathematicalandStatisticalAnalysis = new ArrayList<>();
        ArrayList<JSONObject> StreamNetworkAnalysis = new ArrayList<>();
        for(DscGeoTools dscGeoTools : all){
            JSONObject tool = new JSONObject();
            tool.put("id", dscGeoTools.getId());
            tool.put("label", dscGeoTools.getName());
            tool.put("isLeaf", true);
            switch (dscGeoTools.getType()){
                case "Data Tools":
                    Data_Tools.add(tool);
                    break;
                case "Geomorphometric Analysis":
                    GeomorphometricAnalysis.add(tool);
                    break;
                case "GIS Analysis":
                    GISAnalysis.add(tool);
                    break;
                case "Hydrological Analysis":
                    HydrologicalAnalysis.add(tool);
                    break;
                case "Image Analysis":
                    ImageAnalysis.add(tool);
                    break;
                case "LiDAR Analysis":
                    LiDARAnalysis.add(tool);
                    break;
                case "Mathematical and Statistical Analysis":
                    MathematicalandStatisticalAnalysis.add(tool);
                    break;
                case "Stream Network Analysis":
                    StreamNetworkAnalysis.add(tool);
                    break;
            }
        }
        JSONObject data_tools = new JSONObject();
        data_tools.put("id", IdUtil.objectId());
        data_tools.put("label", "Data Tools");
        data_tools.put("isLeaf", false);
        data_tools.put("children", Data_Tools);
        JSONObject geomorphometric_analysis = new JSONObject();
        geomorphometric_analysis.put("id", IdUtil.objectId());
        geomorphometric_analysis.put("label", "Geomorphometric Analysis");
        geomorphometric_analysis.put("isLeaf", false);
        geomorphometric_analysis.put("children", GeomorphometricAnalysis);
        JSONObject gis_analysis = new JSONObject();
        gis_analysis.put("id", IdUtil.objectId());
        gis_analysis.put("label", "GIS Analysis");
        gis_analysis.put("isLeaf", false);
        gis_analysis.put("children", GISAnalysis);
        JSONObject hydrological_analysis = new JSONObject();
        hydrological_analysis.put("id", IdUtil.objectId());
        hydrological_analysis.put("label", "Hydrological Analysis");
        hydrological_analysis.put("isLeaf", false);
        hydrological_analysis.put("children", HydrologicalAnalysis);
        JSONObject image_analysis = new JSONObject();
        image_analysis.put("id", IdUtil.objectId());
        image_analysis.put("label", "Image Analysis");
        image_analysis.put("isLeaf", false);
        image_analysis.put("children", ImageAnalysis);
        JSONObject lidar_analysis = new JSONObject();
        lidar_analysis.put("id", IdUtil.objectId());
        lidar_analysis.put("label", "LiDAR Analysis");
        lidar_analysis.put("isLeaf", false);
        lidar_analysis.put("children", LiDARAnalysis);
        JSONObject mathematical_and_statistical_analysis = new JSONObject();
        mathematical_and_statistical_analysis.put("id", IdUtil.objectId());
        mathematical_and_statistical_analysis.put("label", "Mathematical and Statistical Analysis");
        mathematical_and_statistical_analysis.put("isLeaf", false);
        mathematical_and_statistical_analysis.put("children", MathematicalandStatisticalAnalysis);
        JSONObject stream_network_analysis = new JSONObject();
        stream_network_analysis.put("id", IdUtil.objectId());
        stream_network_analysis.put("label", "Stream Network Analysis");
        stream_network_analysis.put("isLeaf", false);
        stream_network_analysis.put("children", StreamNetworkAnalysis);
        ArrayList<JSONObject> all_tools = new ArrayList<>();
        all_tools.add(data_tools);
        all_tools.add(geomorphometric_analysis);
        all_tools.add(gis_analysis);
        all_tools.add(hydrological_analysis);
        all_tools.add(image_analysis);
        all_tools.add(lidar_analysis);
        all_tools.add(mathematical_and_statistical_analysis);
        all_tools.add(stream_network_analysis);
        System.out.println(all_tools);
    }
    @Test
    void  testPWD() {
        CommonResult<String> pwd = dscCatalogService.pwd("8174f833-2a40-4cde-8fb5-20ac26f3174f");
        System.out.println("pwd.getData() = " + pwd.getData());
    }

}
