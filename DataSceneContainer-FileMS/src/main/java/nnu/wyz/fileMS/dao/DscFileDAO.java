package nnu.wyz.fileMS.dao;

import nnu.wyz.fileMS.model.entity.DscFileInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DscFileDAO extends MongoRepository<DscFileInfo, String> {

    DscFileInfo findDscFileInfoByCreatedUserAndMd5(String createdUser, String Md5);

}
