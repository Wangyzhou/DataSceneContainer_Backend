package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscPlottingScene;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tjk
 * @date 2025/8/1
 * @Description
 */
@Repository
public interface DscPlottingSceneDAO extends MongoRepository<DscPlottingScene, String> {
    List<DscPlottingScene> findAllByCreatedUser(String createdUser);
}