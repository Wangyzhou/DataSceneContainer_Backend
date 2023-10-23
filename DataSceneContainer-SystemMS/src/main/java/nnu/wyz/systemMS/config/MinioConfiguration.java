//package nnu.wyz.systemMS.config;
//
//import io.minio.MinioClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class MinioConfiguration {
//    @Autowired
//    private MinioConfig minioProp;
//
//    @Bean
//    public MinioClient minioClient()  {
//        return MinioClient.builder()
//                .endpoint(minioProp.getEndpoint())
//                .credentials(minioProp.getAccessKey(), minioProp.getSecretKey())
//                .build();
//    }
//}