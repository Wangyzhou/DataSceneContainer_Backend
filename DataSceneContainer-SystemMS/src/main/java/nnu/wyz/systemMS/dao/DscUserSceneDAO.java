package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscUserScene;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DscUserSceneDAO extends MongoRepository<DscUserScene, String> {

    DscUserScene findByUserIdAndSceneName(String userId, String sceneName);

    List<DscUserScene> findAllByUserId(String userId);

    DscUserScene findByUserIdAndSceneId(String userId, String sceneId);

}
