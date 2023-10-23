package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DscVectorSDAO extends MongoRepository<DscVectorServiceInfo, String> {
    List<DscVectorServiceInfo> findAllByPublisher(String publisher);

    List<DscVectorServiceInfo> findAllByFileId(String fileId);
}
