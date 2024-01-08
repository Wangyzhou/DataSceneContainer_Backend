package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscDASceneConfig;
import nnu.wyz.systemMS.model.entity.DscGDVSceneConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/1/6 18:02
 */
public interface DscDASceneConfigDAO extends MongoRepository<DscDASceneConfig, String> {

    DscDASceneConfig findBySceneId(String sceneId);
}
