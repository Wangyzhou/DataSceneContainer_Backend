package nnu.wyz.systemMS.dao;
import nnu.wyz.systemMS.model.dto.DscWorkflowModelListDTO;
import nnu.wyz.systemMS.model.entity.DscModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tjk
 * @date 2025/1/2
 * @Description
 */
@Repository
public interface DscWorkflowModelDAO extends MongoRepository<DscModel,String> {
    //加载用户所有已经创建的模型
    @Query(value = "{ 'userId' : ?0 }", fields = "{ 'name' : 1, 'id' : 1 }")
    List<DscWorkflowModelListDTO> findDscWorkflowModelsByOwnerId(String ownerId);

    //加载指定Id的模型
    DscModel findDscWorkflowModelById(String id);

}
