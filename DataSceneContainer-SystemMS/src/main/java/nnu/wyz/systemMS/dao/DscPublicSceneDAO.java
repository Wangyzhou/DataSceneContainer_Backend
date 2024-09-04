package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscPublicScene;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/8/30 11:16
 */
public interface DscPublicSceneDAO extends MongoRepository<DscPublicScene, String> {

    DscPublicScene findByName(String name);
}
