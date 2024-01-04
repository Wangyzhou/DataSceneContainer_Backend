package nnu.wyz.systemMS.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/25 14:50
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String sceneThumbnailsBucket;

    private String avatarBucket;
}
