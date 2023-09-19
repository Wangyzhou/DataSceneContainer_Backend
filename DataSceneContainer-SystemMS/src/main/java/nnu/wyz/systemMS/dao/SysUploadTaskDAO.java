package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.SysUploadTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUploadTaskDAO extends MongoRepository<SysUploadTask, String> {

    SysUploadTask findSysUploadTaskByFileId(String fileId);

    SysUploadTask findSysUploadTaskByUploaderAndFileIdentifier(String userId, String md5);
}
