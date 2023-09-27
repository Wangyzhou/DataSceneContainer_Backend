package nnu.wyz.fileMS.dao;

import nnu.wyz.fileMS.model.entity.DscMvtServiceInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DscMvtServiceInfoDAO extends MongoRepository<DscMvtServiceInfo, String> {

    DscMvtServiceInfo findByMvtName(String mvtName);

    List<DscMvtServiceInfo> findAllByFileId(String fileId);
}
