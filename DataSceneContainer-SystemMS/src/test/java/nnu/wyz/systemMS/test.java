package nnu.wyz.systemMS;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscMessageDAO;
import nnu.wyz.systemMS.dao.SysUploadTaskDAO;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.ReturnUsersByEmailLikeDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.server.WebSocketServer;
import nnu.wyz.systemMS.service.DscFileService;
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
import org.springframework.security.core.parameters.P;
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
        GeoJSONUtil.initUtil("D:\\polygon.geojson");
        String geoJSONType = GeoJSONUtil.getGeoJSONType();
        List<Double> geoJSONBBOX = GeoJSONUtil.getGeoJSONBBOX(geoJSONType);
        System.out.println("bbox = " + geoJSONBBOX);
    }

    @Autowired
    private DscGeoJSONService dscGeoJSONService;

    @Test
    void testGetGeoJSON() {
        GeoJSONUtil.initUtil("D:\\global_earthquake.geojson");
        List uniqueValues = GeoJSONUtil.getUniqueValues("date", "asc");
        uniqueValues.forEach(System.out::println);
        List<String> fields = GeoJSONUtil.getFields();
        fields.forEach(System.out::println);
        int featureCount = GeoJSONUtil.getFeatureCount();
        System.out.println("featureCount = " + featureCount);
        List<Map<String, Object>> attrs = GeoJSONUtil.getAttrs();
        attrs.forEach(System.out::println);
    }
}
