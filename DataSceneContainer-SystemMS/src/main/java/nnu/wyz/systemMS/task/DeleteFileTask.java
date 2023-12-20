package nnu.wyz.systemMS.task;

import io.minio.MinioClient;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.SysUploadTaskDAO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.SysUploadTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/23 10:05
 */
@Component
@Slf4j
public class DeleteFileTask {

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private SysUploadTaskDAO sysUploadTaskDAO;

    @Scheduled(cron = "0 0 6 * * ?")
    public void deleteFile(){
        log.info("************定时任务开始执行************");
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
}
