package nnu.wyz.fileMS;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import nnu.wyz.fileMS.model.param.InitTaskParam;
import nnu.wyz.fileMS.service.DscRemoteFileMS;
import nnu.wyz.fileMS.utils.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/5 10:58
 */
@SpringBootTest
public class test {

    @Autowired
    private AmazonS3 amazonS3;
    @Value("${fileSavePath}")
    private String fileRootPath;

    @Autowired
    private DscRemoteFileMS dscRemoteFileMS;
    @Test
    void test1() throws IOException {
//        String bucket = "dsc";
//        String objectKey = "";
//        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, objectKey);
//        S3Object s3Object = amazonS3.getObject(getObjectRequest);
//        String contentType = s3Object.getObjectMetadata().getContentType();
//        if (!contentType.equals("application/zip")) {
//            System.out.println("contentType = " + contentType + ", 不支持解压！");
//        }
        String fullPath = fileRootPath + "test" + "\\" + "2023-08-28\\js_city_region_u.zip";
        File file = new File(fullPath);
        long length = file.length();
        System.out.println("length = " + length);
        String md5 = DigestUtils.md5DigestAsHex(Files.newInputStream(Paths.get(fullPath)));
        System.out.println("md5 = " + md5);
        String name = file.getName();
        System.out.println("name = " + name);
    }

    @Test
    void testUnzip() {
        String fullPath = "C:\\Users\\Administrator\\Downloads\\geoserver-2.23.2-bin.zip";
        try {
            List<JSONObject> jsonObjects = FileUtils.zipUncompress(fullPath, "D:/unZipFiles/testoutput");
            System.out.println("jsonObjects = " + jsonObjects);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testRemote() {
        InitTaskParam initTaskParam = new InitTaskParam();
        initTaskParam.setFileName("hhaha.zip")
                .setIdentifier("nushu123u2h5kn231")
                .setTotalSize(1898291L)
                .setChunkSize(1898291L);
        dscRemoteFileMS.initTask(initTaskParam);
    }

    @Test
    void testCMD() {
        String password = "wyz980903";
//        ProcessBuilder envProcessBuilder;
//        if (System.getProperty("os.name").startsWith("Windows")) {
//            envProcessBuilder = new ProcessBuilder("cmd", "/c", "set", "PGPASSWORD=" + password);
//        } else {
//            envProcessBuilder = new ProcessBuilder("sh", "-c", "export PGPASSWORD=" + password);
//        }
        try {
//            Process envProcess = envProcessBuilder.start();
//            envProcess.waitFor();

            // 执行shp2pgsql命令
            ProcessBuilder shp2pgsqlProcessBuilder = new ProcessBuilder();

            String shp2pgsqlCmd = "D:\\majorSoftware\\PostgreSQL\\10\\bin\\shp2pgsql.exe -I -s 4326 -W LATIN1 E:\\minio-data\\dsc-file\\2023-09-09\\9372c6f0-beec-497a-8b99-62f118b6cfb3.shp js_railway_buffer6445eab15dce250570ab5368_64fc2e024debb11037ccd9e9 | psql -h 172.21.213.86 -p 5432 -U postgres -d dsc_postdb";

            shp2pgsqlProcessBuilder.command("cmd", "/c", shp2pgsqlCmd);

            Map<String, String> environment = shp2pgsqlProcessBuilder.environment();
            environment.put("PGPASSWORD", "wyz980903");

            Process shp2pgsqlProcess = shp2pgsqlProcessBuilder.start();

            // 获取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(shp2pgsqlProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = shp2pgsqlProcess.waitFor();
            System.out.println("Command exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
