package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.SysUploadTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUploadTaskDAO extends MongoRepository<SysUploadTask, String> {

    SysUploadTask findSysUploadTaskByFileId(String fileId);

    SysUploadTask findSysUploadTaskByUploaderAndFileIdentifier(String userId, String md5);

    List<SysUploadTask> findAllByFileId(String fileId);

    List<SysUploadTask> findAllByUploader(String userId);

}
