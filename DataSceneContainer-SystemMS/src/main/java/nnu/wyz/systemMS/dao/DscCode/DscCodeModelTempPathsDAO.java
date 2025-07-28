package nnu.wyz.systemMS.dao.DscCode;

import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModel;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModelTaskTempPaths;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscCodeModelTempPathsDAO extends MongoRepository<DscCodeModelTaskTempPaths, String> {
    DscCodeModelTaskTempPaths findByTaskId(String taskId);
}
