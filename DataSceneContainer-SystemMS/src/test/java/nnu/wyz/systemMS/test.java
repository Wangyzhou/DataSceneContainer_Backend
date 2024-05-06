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
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.DockerClientConfig;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.PythonDockerConfig;
import nnu.wyz.systemMS.config.SagaDockerConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGAInvokeParams;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.ConvertSgrd2GeoTIFFDTO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.PublishTiff2ImageDTO;
import nnu.wyz.systemMS.model.dto.ReturnUsersByEmailLikeDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.model.param.*;
import nnu.wyz.systemMS.service.*;
import nnu.wyz.systemMS.utils.*;
import nnu.wyz.systemMS.websocket.WebSocketServer;
import okhttp3.*;
import org.junit.jupiter.api.Test;
import org.opengis.referencing.FactoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import sun.net.www.http.HttpClient;

import java.io.*;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

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
    void testGetFile() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(minioConfig.getBucketName(), "65f2c2c9e4b04f0617e5a89d/162c5475-33f1-4274-b1e8-c25e5b1dc872.cpg");
        S3Object object = null;
        for (int i = 0; i < 501; i++) {
            object = amazonS3.getObject(getObjectRequest);
            ObjectMetadata objectMetadata = object.getObjectMetadata();
            String eTag = objectMetadata.getETag();
            System.out.println("eTag = " + eTag);
            object.close();
        }
//        for (int i = 0; i < 501; i++) {
//            GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder().bucket(minioConfig.getBucketName()).object("65f2c2c9e4b04f0617e5a89d/162c5475-33f1-4274-b1e8-c25e5b1dc872.cpg").build());
//            System.out.println("object = " + object);
//        }
//        minioClient.createMultipartUpload(bucketName, key);
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

//    @Test
//    void testCatalogTree() {
//        String catalogId = "1bd88385-4252-42f2-ab19-751a6b294da8";
//        String userId = "64eda0524debf898422c7919";
//        List<JSONObject> precursion = precursion(catalogId);
//        System.out.println("precursion = " + precursion);
//    }
//
//    List<JSONObject> precursion(String catalogId) {
//        Optional<DscCatalog> byId = dscCatalogDAO.findById(catalogId);
//        DscCatalog dscCatalog = byId.get();
//        if (dscCatalog.getChildren().size() == 0) {
//            return null;
//        }
//        ArrayList<JSONObject> catalogItems = new ArrayList<>();
//        for (CatalogChildrenDTO childrenDTO :
//                dscCatalog.getChildren()) {
//            if (childrenDTO.getType().equals("folder")) {
//                JSONObject jsonObject = new JSONObject();
//                jsonObject.put("id", childrenDTO.getId());
//                jsonObject.put("label", childrenDTO.getName());
//                List<JSONObject> precursion = precursion(childrenDTO.getId());
//                if (precursion != null) {
//                    jsonObject.put("children", precursion);
//                }
//                catalogItems.add(jsonObject);
//            }
//        }
//        return catalogItems;
//    }

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
        message.setTo("1132691603@qq.com");
        message.setTopic("test");
        message.setType("tool-execute");
        message.setText("hahaha");
//        webSocketServer.sendInfo("652a48fde4b01213a180bb5a", JSON.toJSONString(message));
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
        dscUser.setEmail("adminmfz");
        dscUser.setPassword(bcryptPasswordEncoder.encode("123"));
        dscUser.setUserName("admin");
        dscUser.setInstitution("nnu");
        dscUser.setRegisterDate(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
//        dscUser.setEnabled(1);
        String activeCode = RandomUtil.randomString(5);
        dscUser.setActiveCode(activeCode);
        dscUserDAO.insert(dscUser);
//        dscCatalogService.createRootCatalog(dscUser.getId());
    }

    @Test
    void setSceneDataRootCatalog() {
        dscCatalogService.createSceneDataRootCatalog("652a4b75e4b01213a180bb5b");
    }

    @Test
    void test2122() {
        CommonResult<List<JSONObject>> catalogChildrenTree = dscCatalogService.getCatalogChildrenTree("ce344b9e-0b68-46b1-9765-e50922855b6f");
        System.out.println("catalogChildrenTree = " + catalogChildrenTree.getData());
    }

    @Test
    void testSortCatalog() {
        CommonResult<List<CatalogChildrenDTO>> children = dscCatalogService.getChildren("ce344b9e-0b68-46b1-9765-e50922855b6f");
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
        pageableDTO.setCriteria("b9eb6e25-70d2-4802-9789-caacff49125a");
        pageableDTO.setKeyword("");
        pageableDTO.setPageIndex(1);
        pageableDTO.setPageSize(10);
        CommonResult<PageInfo<CatalogChildrenDTO>> childrenByPageable = dscCatalogService.getChildrenByPageable(pageableDTO);
        PageInfo<CatalogChildrenDTO> data = childrenByPageable.getData();
        System.out.println("data = " + data);
    }

    @Autowired
    private DscVectorSService dscVectorSService;

    @Test
    void testGetVectorServiceList() {
        int pageIndex = 1;
        int pageSize = 4;
        PageableDTO pageableDTO = new PageableDTO("65f3b12ae4b0d760656a8329", "", pageIndex, pageSize);
        CommonResult<PageInfo<DscVectorServiceInfo>> vectorSList = dscVectorSService.getVectorServiceList(pageableDTO);
        System.out.println(vectorSList.getData());
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
        PageableDTO pageableDTO = new PageableDTO("65f3b12ae4b0d760656a8329", "", pageIndex, pageSize);
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

//    @Autowired
//    private DscGeoToolsDAO dscGeoToolsDAO;

//    @Test
//    void testGeoTools() {
//        List<DscGeoTools> all = dscGeoToolsDAO.findAll();
//        System.out.println("all = " + all.get(0));
//        ArrayList<JSONObject> Data_Tools = new ArrayList<>();
//        ArrayList<JSONObject> GeomorphometricAnalysis = new ArrayList<>();
//        ArrayList<JSONObject> GISAnalysis = new ArrayList<>();
//        ArrayList<JSONObject> HydrologicalAnalysis = new ArrayList<>();
//        ArrayList<JSONObject> ImageAnalysis = new ArrayList<>();
//        ArrayList<JSONObject> LiDARAnalysis = new ArrayList<>();
//        ArrayList<JSONObject> MathematicalandStatisticalAnalysis = new ArrayList<>();
//        ArrayList<JSONObject> StreamNetworkAnalysis = new ArrayList<>();
//        for (DscGeoTools dscGeoTools : all) {
//            JSONObject tool = new JSONObject();
//            tool.put("id", dscGeoTools.getId());
//            tool.put("label", dscGeoTools.getName());
//            tool.put("isLeaf", true);
//            switch (dscGeoTools.getType()) {
//                case "Data Tools":
//                    Data_Tools.add(tool);
//                    break;
//                case "Geomorphometric Analysis":
//                    GeomorphometricAnalysis.add(tool);
//                    break;
//                case "GIS Analysis":
//                    GISAnalysis.add(tool);
//                    break;
//                case "Hydrological Analysis":
//                    HydrologicalAnalysis.add(tool);
//                    break;
//                case "Image Analysis":
//                    ImageAnalysis.add(tool);
//                    break;
//                case "LiDAR Analysis":
//                    LiDARAnalysis.add(tool);
//                    break;
//                case "Mathematical and Statistical Analysis":
//                    MathematicalandStatisticalAnalysis.add(tool);
//                    break;
//                case "Stream Network Analysis":
//                    StreamNetworkAnalysis.add(tool);
//                    break;
//            }
//        }
//        JSONObject data_tools = new JSONObject();
//        data_tools.put("id", IdUtil.objectId());
//        data_tools.put("label", "Data Tools");
//        data_tools.put("isLeaf", false);
//        data_tools.put("children", Data_Tools);
//        JSONObject geomorphometric_analysis = new JSONObject();
//        geomorphometric_analysis.put("id", IdUtil.objectId());
//        geomorphometric_analysis.put("label", "Geomorphometric Analysis");
//        geomorphometric_analysis.put("isLeaf", false);
//        geomorphometric_analysis.put("children", GeomorphometricAnalysis);
//        JSONObject gis_analysis = new JSONObject();
//        gis_analysis.put("id", IdUtil.objectId());
//        gis_analysis.put("label", "GIS Analysis");
//        gis_analysis.put("isLeaf", false);
//        gis_analysis.put("children", GISAnalysis);
//        JSONObject hydrological_analysis = new JSONObject();
//        hydrological_analysis.put("id", IdUtil.objectId());
//        hydrological_analysis.put("label", "Hydrological Analysis");
//        hydrological_analysis.put("isLeaf", false);
//        hydrological_analysis.put("children", HydrologicalAnalysis);
//        JSONObject image_analysis = new JSONObject();
//        image_analysis.put("id", IdUtil.objectId());
//        image_analysis.put("label", "Image Analysis");
//        image_analysis.put("isLeaf", false);
//        image_analysis.put("children", ImageAnalysis);
//        JSONObject lidar_analysis = new JSONObject();
//        lidar_analysis.put("id", IdUtil.objectId());
//        lidar_analysis.put("label", "LiDAR Analysis");
//        lidar_analysis.put("isLeaf", false);
//        lidar_analysis.put("children", LiDARAnalysis);
//        JSONObject mathematical_and_statistical_analysis = new JSONObject();
//        mathematical_and_statistical_analysis.put("id", IdUtil.objectId());
//        mathematical_and_statistical_analysis.put("label", "Mathematical and Statistical Analysis");
//        mathematical_and_statistical_analysis.put("isLeaf", false);
//        mathematical_and_statistical_analysis.put("children", MathematicalandStatisticalAnalysis);
//        JSONObject stream_network_analysis = new JSONObject();
//        stream_network_analysis.put("id", IdUtil.objectId());
//        stream_network_analysis.put("label", "Stream Network Analysis");
//        stream_network_analysis.put("isLeaf", false);
//        stream_network_analysis.put("children", StreamNetworkAnalysis);
//        ArrayList<JSONObject> all_tools = new ArrayList<>();
//        all_tools.add(data_tools);
//        all_tools.add(geomorphometric_analysis);
//        all_tools.add(gis_analysis);
//        all_tools.add(hydrological_analysis);
//        all_tools.add(image_analysis);
//        all_tools.add(lidar_analysis);
//        all_tools.add(mathematical_and_statistical_analysis);
//        all_tools.add(stream_network_analysis);
//        System.out.println(all_tools);
//    }

    @Test
    void testPWD() {
        CommonResult<String> pwd = dscCatalogService.pwd("8174f833-2a40-4cde-8fb5-20ac26f3174f");
        System.out.println("pwd.getData() = " + pwd.getData());
    }

    static Object lock = new Object();

    @Autowired
    private SagaDockerConfig sagaDockerConfig;

    @Test
    void testDocker() throws InterruptedException, IOException {
//        List<Container> exec = dockerClient.listContainersCmd().exec();
//        exec.forEach(System.out::println);
        DockerClient dockerClient = sagaDockerConfig.getDockerClient();
        ExecCreateCmdResponse exec1 = dockerClient.execCreateCmd("e904431d1b38ec6fba77361019321875536deaf0169ebd6872af66bbab67d879")
                .withCmd("whitebox_tools", "--help")
                .withTty(true)
                .exec();
        dockerClient.execStartCmd(exec1.getId())
                .withDetach(false).withTty(true)
                .exec(new ExecStartResultCallback(System.out, System.err)).awaitCompletion();
    }

    @Test
    void testDocker2() throws IOException, InterruptedException {
//        String snippet = "hello world";
//        Volume volume = new Volume("/home/yzwang/whitebox_workdir/");
//        LogConfig logConfig = new LogConfig(LogConfig.LoggingType.SYSLOG, null);
//        CreateContainerResponse container = dockerClient.createContainerCmd("yzwang/whitebox_tools")
//                .withCmd("bash")
//                .withTty(false)
//                .withUser("root")
//                .withAttachStdin(true)
//                .withAttachStdout(true)
//                .withAttachStderr(true)
//                .withStdinOpen(true)
//                .withBinds(Bind.parse("/home/yzwang/whitebox_workdir:/whitebox_workdir"))
//                .exec();
//        dockerClient.startContainerCmd(container.getId()).exec();
        String[] arr = new String[]{"saga_cmd", "shapes_tools", "18", "-SHAPES=/home/minio-data/dsc-file/652a48fde4b01213a180bb5a/55a9453b-f5fe-4c16-848f-c83f1e50c99c.shp", "-BUFFER=/home/minio-data/dsc-file/test_buffer"};
        DockerClient dockerClient = sagaDockerConfig.getDockerClient();

        ExecCreateCmdResponse whiteboxTools = dockerClient.execCreateCmd("e904431d1b38ec6fba77361019321875536deaf0169ebd6872af66bbab67d879")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(arr)
                .exec();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        dockerClient.execStartCmd(whiteboxTools.getId())
                .exec(new ExecStartResultCallback(stdout, stderr) {
                    @Override
                    public void onNext(Frame frame) {
//                        if(frame.toString().contains("Save")) {
//                            return;
//                        }
                        System.out.println(frame.toString().replace("STDOUT: ", "").replace("STDERR: ", ""));
                        super.onNext(frame);
                    }
                })
                .awaitCompletion();
//        System.out.println("stdout = " + stdout.toString());
//        System.out.println("stderr = " + stderr.toString());
//        dockerClient.stopContainerCmd(container.getId()).exec();
//        dockerClient.removeContainerCmd(container.getId()).exec();
    }

    @Test
    void testgetCatalogPhysicalPath() {
//        String physicalPath = dscCatalogService.getPhysicalPath("8174f833-2a40-4cde-8fb5-20ac26f3174f");
//        System.out.println("physicalPath = " + physicalPath);
    }

    @Test
    void testWinDocker() throws InterruptedException {
        String[] arr = new String[]{"ip", "addr"};
        DockerClient dockerClient = sagaDockerConfig.getDockerClient();
        ExecCreateCmdResponse exec = dockerClient.execCreateCmd("2785cdc9e39cafece987c54ad8ee4c13b19e3e2905bfa1f11aeb8388bad2973e")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(arr)
                .exec();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        dockerClient.execStartCmd(exec.getId())
                .exec(new ExecStartResultCallback(stdout, stderr) {
                    @Override
                    public void onNext(Frame frame) {
//                        if(frame.toString().contains("Save")) {
//                            return;
//                        }
                        System.out.println(frame.toString().replace("STDOUT: ", "").replace("STDERR: ", ""));
                        super.onNext(frame);
                    }
                })
                .awaitCompletion();
    }

    @Test
    void testExecute() {
        DscInvokeToolParams dscInvokeToolParams = new DscInvokeToolParams();
        dscInvokeToolParams.setToolId("656dcc54ccc545e844ef6071");
        dscInvokeToolParams.setUserId("652a48fde4b01213a180bb5a");
        DscToolRawParams p1 = new DscToolRawParams("Input DEM", "657ab0bae4b0f9826e8f799b", null);
        DscToolRawParams p2 = new DscToolRawParams("Output File", "aspect.tif", "4f81318a-172e-44d2-ba12-74779f0422f3");
        DscToolRawParams p3 = new DscToolRawParams("Z Conversion Factor", null, null);
        ArrayList<DscToolRawParams> dscToolRawParams = new ArrayList<>();
        dscToolRawParams.add(p1);
        dscToolRawParams.add(p2);
        dscToolRawParams.add(p3);
        dscInvokeToolParams.setToolRawParams(dscToolRawParams);
        DscGeoToolExecTask dscGeoToolExecTask = new DscGeoToolExecTask();

    }

    @Autowired
    private DscGeoAnalysisDAO dscGeoAnalysisDAO;

    @Test
    void getDscGATool() {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById("ce344b9e-0b68-46b1-9765-e50922855b6f");
        if (byId.isPresent()) {
            System.out.println("byId = " + byId.get());
        }
    }

    @Autowired
    private DscGeoAnalysisTaskService dscGeoAnalysisTaskService;

    @Test
    void testGA() throws InterruptedException {
        DscGAInvokeParams dscGAInvokeParams = new DscGAInvokeParams();
        dscGAInvokeParams.setToolId("ce344b9e-0b68-46b1-9765-e50922855b6f");
        dscGAInvokeParams.setSceneCatalog("adb52290-36e4-487c-96eb-736d54351fc8");
        dscGAInvokeParams.setExecutor("652a48fde4b01213a180bb5a");
        HashMap<String, String> input = new HashMap<>();
        input.put("Shapes", "652a5f07e4b012905c858bee");
        HashMap<String, Object> options = new HashMap<>();
        options.put("Default", 100);
        options.put("Scaling Factor for Attribute Value", 1);
        options.put("Dissolve Buffers", "1");
        options.put("Number of Buffer Zones", 1);
        options.put("Inner Buffer", "0");
        options.put("Arc Vertex Distance [Degree]", 5);
        dscGAInvokeParams.setInput(input);
        dscGAInvokeParams.setOptions(options);
        CommonResult<DscGeoAnalysisExecTask> dscGeoAnalysisExecTaskCommonResult = dscGeoAnalysisTaskService.submitGATask(dscGAInvokeParams);
        System.out.println(dscGAInvokeParams);
        while (true) {
            synchronized (lock) {
                // 除非有线程唤醒他 lock.notify();
                lock.wait();
            }
        }

    }

    @Autowired
    private DscRasterSService dscRasterSService;

    @Test
    void testPublishTiff() {
        PublishTiff2ImageDTO publishTiff2ImageDTO = new PublishTiff2ImageDTO();
        publishTiff2ImageDTO.setUserId("652a5e61e4b012905c858bea");
        publishTiff2ImageDTO.setFileId("65b211ffe4b08e2b13be0131");
        publishTiff2ImageDTO.setName("test");
        publishTiff2ImageDTO.setOutputCatalogId("d4c1b985-cce4-48bc-925b-71a0d7ba0545");
        publishTiff2ImageDTO.setMethod("scene");
        dscRasterSService.publishTiff2RasterS(publishTiff2ImageDTO);
    }

    @Test
    void testSaga() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", "D:\\majorSoftware\\saga-7.3.0_x64\\saga_cmd.exe", "-h", "shapes_tools", "18");
        try {
            Process start = processBuilder.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(start.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while (((line = br.readLine()) != null)) {
                System.out.println(line);
                sb.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private PythonDockerConfig pythonDockerConfig;

    private static final String PYTHON_CONTAINER_ID = "60d00132d32e49dc192d123f7c6fe1cc4790e938078e2df314d5570aed16404f";
    @Autowired
    private DscGeoAnalysisToolService dscGeoAnalysisToolService;

    @Test
    void testSagaDocker() {
        ConvertSgrd2GeoTIFFDTO convertSgrd2GeoTIFFDTO = new ConvertSgrd2GeoTIFFDTO();
        convertSgrd2GeoTIFFDTO.setSgrdFile("65b0bc59e4b066b41f7a45bb");
        convertSgrd2GeoTIFFDTO.setOutputDir("bd204d16-c46b-4bed-994f-190bc91bd61a");
        convertSgrd2GeoTIFFDTO.setUserId("652a48fde4b01213a180bb5a");
        dscGeoAnalysisToolService.convertSgrd2Geotiff(convertSgrd2GeoTIFFDTO);
    }

    @Autowired
    DscRasterSDAO dscRasterSDAO;

    @Test
    void testGetRsbyfileIdOrOriFileId() {
        List<DscRasterService> allByFileId = dscRasterSDAO.findAllByFileIdOrOriFileId("65b211ffe4b08e2b13be0131");
        System.out.println(allByFileId);
    }

    @Test
    void testGetGeoAnalysisTool() {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById("d69c9af4-dac0-11ee-bc5a-f44efc6889fd");
        if (byId.isPresent()) {
            System.out.println(byId.get());
        }
    }

    @Test
    void testMinioClient() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        System.out.println(minioConfig.getEndpoint());

        MinioClient minioClient = MinioClient.builder()
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .endpoint("http://119.45.181.127:82")
                .build();
        List<io.minio.messages.Bucket> buckets = minioClient.listBuckets();
        buckets.forEach(bucket -> System.out.println(bucket.name()));
    }

    @Test
    void testCompressImage() throws IOException {
        File file = new File("F:\\ea6c7543-8090-494d-ad40-a74b05e5417a.png");
        MultipartFile file1 = ImageUtil.compressImageFile(file);
        System.out.println(file1.getSize());
    }

    @Test
    void testGeoTools() throws IOException, FactoryException {
        GeoToolsUtil.init("E:\\\\GeoserverTestData\\\\caijianhou2.tif");
        String tiffEpsgCode = GeoToolsUtil.getTiffEpsgCode();
        System.out.println("tiffEpsgCode = " + tiffEpsgCode);
        List<Double> tiffBbox = GeoToolsUtil.getTiffBbox();
        System.out.println("tiffBbox = " + tiffBbox);
//        File file = new File("E:\\GeoserverTestData\\caijianhou2.tif");
//        AbstractGridFormat format = GridFormatFinder.findFormat( file );
//        GridCoverage2DReader reader = format.getReader( file );
//        GridCoverage2D coverage = reader.read(null);
//        CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
//        String crsWKTStr = crs.toWKT();
//        Map<String, String> map = extractEPSG(crsWKTStr);
//        System.out.println("map = " + map.get("EPSG"));
        //resample参数修改
//        Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
//        CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
//        CoordinateReferenceSystem new_crs = factory.createCoordinateReferenceSystem("EPSG:4326");
//        GridCoverage2D newCoverage2D = (GridCoverage2D) Operations.DEFAULT.resample(coverage, new_crs);
//        Envelope2D envelope2D = newCoverage2D.getEnvelope2D();
//        double maxX = envelope2D.getMaxX();
//        double minX = envelope2D.getMinX();
//        double maxY = envelope2D.getMaxY();
//        double minY = envelope2D.getMinY();
//        final File writeFile =
//                new File(
//                        new StringBuilder("E:/GeoserverTestData/output/")
//                                .append(File.separatorChar)
//                                .append(newCoverage2D.getName().toString())
//                                .append(".png")
//                                .toString());
//        final GridCoverageWriter writer = format.getWriter(writeFile);
//
//        try {
//            writer.write(newCoverage2D, null);
//        } catch (IOException ignored) {
//        } finally {
//            try {
//                writer.dispose();
//            } catch (Throwable ignored) {
//            }
//        }
//        String s = crs.toWKT();
//        System.out.println("crs = " + s);
//        Envelope env = coverage.getEnvelope();
//        System.out.println("env = " + env);
//        RenderedImage image = coverage.getRenderedImage();
    }

    public static Map<String, String> extractEPSG(String crsWKTStr) {
        Map<String, String> map = new HashMap<>();
        String pattern = "AUTHORITY\\[\"(\\w+)\",\"(\\d+)\"\\]";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(crsWKTStr);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            map.put(key, value);
        }
        return map;
    }

    @Autowired
    private DscComputeContainerInstanceDAO dscComputeContainerInstanceDAO;

    @Test
    void testInitComputeContainer() throws InterruptedException {
        /*
         * 初始化计算容器
         * 1、
         */
//        pythonDockerConfig.getDockerClient().startContainerCmd("377c9eca57efed5b016247e8083b28ff6ed3178d23d06ff979acd9a7373917ff").exec();

//        HostConfig hostConfig = new HostConfig();
//        Bind bind = new Bind("/home/yzwang/dsc/dsc-minio/data",new Volume("/home/minio-data"));
//        hostConfig.setBinds(bind);
        CreateContainerResponse testComputeContainer = pythonDockerConfig.getDockerClient().createContainerCmd("fangzhuom/dsc-tools:v1.0.0")
//                .withHostConfig(hostConfig)
//                .withCmd("init")
                .withName("testComputeContainer")
                .exec();
        dockerClientConfig.getDockerClient().startContainerCmd(testComputeContainer.getId()).exec();

//        InspectContainerResponse test = pythonDockerConfig.getDockerClient().inspectContainerCmd("ef28e4eee22503de5cd9252c69ba031d8a1f37f2b47aaa90e9c90b2f5810af4a").exec();
//        String image = test.getConfig().getImage();
//        System.out.println("image = " + image);
//        String targetImage = "yzwang98/dsc-saga-module:saga8.2.0";
//        List<Image> images = pythonDockerConfig.getDockerClient().listImagesCmd().exec();
//        List<String> collect = images.stream().map(image -> image.getRepoTags()).map(repoTags -> Arrays.toString(repoTags)).filter(repoTagArrStr -> repoTagArrStr.contains(targetImage)).collect(Collectors.toList());
//        if(collect.size() == 0) {    // 不存在镜像
//            try {
//                pythonDockerConfig.getDockerClient().pullImageCmd(targetImage).start().awaitCompletion();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        // 拉取私人仓库镜像
//        AuthConfig authConfig = new AuthConfig()
//                .withUsername("wyz980903@163.com")
//                .withPassword("Ninja19981022..")
//                .withEmail("wyz980903@1623.com");
//        pythonDockerConfig.getDockerClient().pullImageCmd("yzwang98/dsc.minio:latest")
//                .withAuthConfig(authConfig)
//                .start().awaitCompletion();

    }

    @Autowired
    private DockerClientConfig dockerClientConfig;

    @Test
    void testFilterLocalImages() {
        List<Image> localImages = dockerClientConfig.getDockerClient().listImagesCmd().withShowAll(true).exec();
        ArrayList<String> localImageNames = new ArrayList<>();
        localImages.stream().map(Image::getRepoTags).filter(Objects::nonNull).flatMap(Arrays::stream).forEach(localImageNames::add); // 将每个元素添加到 localImageIds
        localImageNames.forEach(System.out::println);
    }

    @Test
    void testOllama() {
        String question = "Why does the sky is blue?";
        String[] cmds = {"curl", "-X", "POST", "http://localhost:11434/api/generate", "-d", "{\\\"model\\\": \\\"llama2\\\", \\\"prompt\\\":\\\"" + question + "\\\"}", "-H", "Content-Type: application/json"};
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(cmds);
        Process process;
        try {
            process = pb.start();
            BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = bf.readLine()) != null) {
//                JSONObject jsonObject = JSON.parseObject(line);
//                if((boolean)jsonObject.get("done")) {
//                    break;
//                }
//                System.out.println(jsonObject.get("response"));
                System.out.println(line);
            }
//            bf.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final MediaType JSON = MediaType.get("application/json");

    @Test
    void testOllama2() {
        String question = "{'tools': [{'id': '864b93fa-c97d-7883-3382-71072c4fac94', 'name': '15-minutes living area model', 'params': {'options': [{'name': 'Routing', 'description': 'mode of travel, 0 for driving, 1 for driving-traffic, 2 for walking, 3 for cycling', 'defaultVal': '2'}, {'name': 'Contour', 'description': 'when coutour type is minutes, it represents time; when coutour type is meters, it represents distance.', 'defaultVal': '20'}, {'name': 'Countour type', 'description': 'measurement, 0 for minutes, 1 for meters', 'defaultVal': '0'}], 'inputs': [{'name': 'District', 'description': 'district data in study area.'}, {'name': 'Community', 'description': 'community data in study area.'}, {'name': 'POIs', 'description': 'POI data in study area.'}]}}]}";
        String data = "{\"model\": \"llama3:8b\", \"prompt\": \"" + question + "\", \"system\": \"You are a model management assistant and have been asked to describe the model to the user based on the given data.\", \"options\": {\"temperature\": 0.1}}";
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        // 请求体 根据自己的需求更换get、post请求及请求变量
        RequestBody body = RequestBody.create(data, JSON);
        Request request = new Request.Builder()
                .url("http://172.21.252.160:11434/api/generate")
                .method("POST", body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            //执行成功
            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                StringBuilder output = new StringBuilder();
                while ((line = bf.readLine()) != null) {
                    JSONObject jsonObject = com.alibaba.fastjson.JSON.parseObject(line);
                    System.out.println(line);
                    output.append(jsonObject.get("response"));
                }
                bf.close();
                System.out.println(output);
            } else {
                System.out.println(response.body().string());
            }
        } catch (IOException e) {
            // 处理IO异常
            e.printStackTrace();
        }
    }

    @Test
    void testPythonOllama() {
        String pyPath = "F:\\数据场景容器相关\\Code_sz_0419\\Code_sz_0419\\main_for_one_chat.py";
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("python", pyPath, "-MODEL", "llama3:8b", "-IDENTIFIER", "2e05fce0-34bf-4e5e-a134-1fd567153a53", "-PROMPT", "I would like to do a simulation of NanJing's 15-minute living area, please let me know the available models.");
        Process pro;
        BufferedReader bf = null;
        try {
            pro = processBuilder.start();
            bf = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
                // websocket
                Message message = new Message();
                message.setFrom("system")
                        .setTo("1132691603@qq.com")
                        .setType("ai-chat")
                        .setTopic("response")
                        .setText(line)
                        .setIsRead(false)
                        .setIsFinished(false);
                webSocketServer.sendInfo("1132691603@qq.com", JSONObject.toJSONString(message));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    @Test
    void testDockerOllama() {
        String containerId = "e3ea9f45b1f04df523b26b12ceed1dfcd50a0ee4685c7ef52ef617d62df5c340";
        DockerClient dockerClient = dockerClientConfig.getDockerClient();
        ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerId)
                .withCmd("python", "main_for_one_chat.py", "-MODEL", "llama3:8b", "-IDENTIFIER", "2e05fce0-34bf-4e5e-a134-1fd567153a53", "-PROMPT", "I would like to do a simulation of NanJing's 15-minute living area, please let me know the available models.")
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream stderr = new PrintStream(baos);
        try {
            dockerClient.execStartCmd(exec.getId())
                    .exec(new ExecStartResultCallback(stdout,stderr) {
                        @Override
                        public void onNext(Frame frame) {
                            System.out.println(frame.toString().replace("STDOUT: ", ""));
                            super.onNext(frame);
                        }
                    }).awaitCompletion();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            stderr.close();
            try {
                baos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
    @Test
    void testGetCatalogByRootAndFileId() {
//        DscCatalog dscCatalogById = dscCatalogDAO.findDscCatalogById("e44e9f12-3ea6-4146-985c-7415b4e85732");
//        System.out.println(dscCatalogById.toString());
        CommonResult<String> catalogIdByFileIdAndRoot = dscCatalogService.getCatalogIdByFileIdAndRoot("e44e9f12-3ea6-4146-985c-7415b4e85732", "6628710de4b04d5bc14a80d0");
        System.out.println(catalogIdByFileIdAndRoot.getData());
    }
}
