package nnu.wyz.systemMS;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.utils.MimeTypesUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 17:00
 */
@SpringBootTest
public class test {

    @Autowired
    private AmazonS3 amazonS3;
    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;

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
}
