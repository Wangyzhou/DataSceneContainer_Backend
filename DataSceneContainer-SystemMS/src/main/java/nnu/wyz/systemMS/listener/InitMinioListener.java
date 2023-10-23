package nnu.wyz.systemMS.listener;

import com.amazonaws.services.s3.AmazonS3;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.systemMS.config.MinioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/23 14:12
 */
@Component
@Slf4j
public class InitMinioListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("*******初始化minio*******");
        boolean isFileBucketExist = amazonS3.doesBucketExistV2(minioConfig.getBucketName());
        if (!isFileBucketExist) {
            amazonS3.createBucket(minioConfig.getBucketName());
            log.info("创建文件桶: " + minioConfig.getBucketName());
        }
        boolean isSceneThumbnailsBucketExist = amazonS3.doesBucketExistV2(minioConfig.getSceneThumbnailsBucket());
        if (!isSceneThumbnailsBucketExist) {
            amazonS3.createBucket(minioConfig.getSceneThumbnailsBucket());
            log.info("创建场景缩略图桶: " + minioConfig.getSceneThumbnailsBucket());
        }
        log.info("*******初始化minio完成*******");
    }
}
