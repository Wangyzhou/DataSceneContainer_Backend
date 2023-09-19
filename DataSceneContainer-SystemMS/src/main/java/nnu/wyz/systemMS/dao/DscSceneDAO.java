package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscScene;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscSceneDAO extends MongoRepository<DscScene, String> {


}
