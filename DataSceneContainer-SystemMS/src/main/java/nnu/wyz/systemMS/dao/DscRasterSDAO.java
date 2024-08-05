package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DscRasterSDAO extends MongoRepository<DscRasterService, String> {
    DscRasterService findDscRasterServiceById(String rasterSId);

    List<DscRasterService> findAllByOwnerCount(Long ownerCount);

    @Query("{$or:[{'fileId': ?0}, {'oriFileId': ?0}]}")
    List<DscRasterService> findAllByFileIdOrOriFileId(String fileId);

    @Query("{'_id': {$in: ?0}}")
    List<DscRasterService> findAllByIds(List<String> ids);

    <S extends DscRasterService> List<S> saveAll(Iterable<S> entities);
}
