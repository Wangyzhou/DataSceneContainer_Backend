package nnu.wyz.systemMS.listener;

import com.amazonaws.services.s3.AmazonS3;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.systemMS.config.MinioConfig;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        log.info("*******初始化minio*******");
        String fileBucketPolicy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::" + minioConfig.getBucketName() + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\"],\"Resource\":[\"arn:aws:s3:::" + minioConfig.getBucketName() + "/*\"]}]}";
        String sceneThumbnailsBucketPolicy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::" + minioConfig.getSceneThumbnailsBucket() + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\"],\"Resource\":[\"arn:aws:s3:::" + minioConfig.getSceneThumbnailsBucket() + "/*\"]}]}";
        String avatarBucketPolicy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetBucketLocation\",\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"],\"Resource\":[\"arn:aws:s3:::" + minioConfig.getAvatarBucket() + "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\"],\"Resource\":[\"arn:aws:s3:::" + minioConfig.getAvatarBucket() + "/*\"]}]}";
        MinioClient minioClient = MinioClient.builder()
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .endpoint(minioConfig.getEndpoint())
                .build();
        boolean isFileBucketExist;
        boolean isSceneThumbnailsBucketExist;
        boolean isAvatarBucketExist;
        try {
            isFileBucketExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());
            isSceneThumbnailsBucketExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getSceneThumbnailsBucket()).build());
            isAvatarBucketExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getAvatarBucket()).build());
            if (!isFileBucketExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build());
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(minioConfig.getBucketName()).config(fileBucketPolicy).build());
                log.info("创建文件桶: " + minioConfig.getBucketName());
            }
            if (!isSceneThumbnailsBucketExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getSceneThumbnailsBucket()).build());
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(minioConfig.getSceneThumbnailsBucket()).config(sceneThumbnailsBucketPolicy).build());
                log.info("创建场景缩略图桶: " + minioConfig.getSceneThumbnailsBucket());
            }
            if (!isAvatarBucketExist) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfig.getAvatarBucket()).build());
                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(minioConfig.getAvatarBucket()).config(avatarBucketPolicy).build());
                log.info("创建头像桶: " + minioConfig.getAvatarBucket());
            }
            log.info("*******初始化minio完成*******");
        } catch (ErrorResponseException | InternalException | InsufficientDataException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            e.printStackTrace();
            log.error("文件桶创建失败");
        }
    }
}
