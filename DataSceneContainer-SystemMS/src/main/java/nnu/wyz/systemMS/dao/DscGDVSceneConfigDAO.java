package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscGDVSceneConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscGDVSceneConfigDAO extends MongoRepository<DscGDVSceneConfig, String> {

    DscGDVSceneConfig findBySceneId(String sceneId);

}
