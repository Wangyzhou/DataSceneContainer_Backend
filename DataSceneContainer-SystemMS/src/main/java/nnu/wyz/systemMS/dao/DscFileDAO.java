package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface DscFileDAO extends MongoRepository<DscFileInfo, String> {

    List<DscFileInfo> findAllByOwnerCount(Long ownerCount);

    List<DscFileInfo> findAllByOwnerCountAndPublishCount(Long ownerCount,Long publishCount);

    List<DscFileInfo> findAllByCreatedUser(String userId);



    @Query("{'_id': {$in: ?0}}")
    List<DscFileInfo> findAllByIds(List<String> ids);
}
