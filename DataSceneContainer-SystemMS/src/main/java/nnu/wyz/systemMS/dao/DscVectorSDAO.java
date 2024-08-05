package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DscVectorSDAO extends MongoRepository<DscVectorServiceInfo, String> {
    List<DscVectorServiceInfo> findAllByPublisher(String publisher);

    List<DscVectorServiceInfo> findAllByOwnerCount(Long ownerCount);

    List<DscVectorServiceInfo> findAllByFileId(String fileId);

    @Query("{'_id': {$in: ?0}}")
    List<DscVectorServiceInfo> findAllByIds(List<String> ids);

    <S extends DscVectorServiceInfo> List<S> saveAll(Iterable<S> entities);

}
