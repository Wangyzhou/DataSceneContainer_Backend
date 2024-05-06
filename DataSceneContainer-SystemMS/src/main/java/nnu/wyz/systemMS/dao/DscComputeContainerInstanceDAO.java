package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscComputeContainerInstance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/4/10 15:31
 */
@Repository
public interface DscComputeContainerInstanceDAO extends MongoRepository<DscComputeContainerInstance, String> {

    List<DscComputeContainerInstance> findAllByImageId(String imageId);
}
