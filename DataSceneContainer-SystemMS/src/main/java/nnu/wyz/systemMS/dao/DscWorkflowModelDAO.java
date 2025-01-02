package nnu.wyz.systemMS.dao;
import nnu.wyz.systemMS.model.entity.DscCodeFile;
import nnu.wyz.systemMS.model.entity.DscWorkflowModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tjk
 * @date 2025/1/2
 * @Description
 */
@Repository
public interface DscWorkflowModelDAO extends MongoRepository<DscWorkflowModel,String> {
    //加载用户所有已经创建的模型
    List<DscWorkflowModel> findDscWorkflowModelsByUserId(String userId);

    //加载指定Id的模型
    DscWorkflowModel findDscWorkflowModelById(String id);

}
