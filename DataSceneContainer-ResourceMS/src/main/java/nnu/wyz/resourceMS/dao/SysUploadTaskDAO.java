package nnu.wyz.resourceMS.dao;

import nnu.wyz.resourceMS.model.entity.SysUploadTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUploadTaskDAO extends MongoRepository<SysUploadTask, String> {

    SysUploadTask findSysUploadTaskByFileIdentifier(String identifier);
}
