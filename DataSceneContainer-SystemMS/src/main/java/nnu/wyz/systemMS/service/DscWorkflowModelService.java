package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscWorkflowModelDTO;
import nnu.wyz.systemMS.model.dto.DscWorkflowModelListDTO;
import nnu.wyz.systemMS.model.entity.DscModel;

import java.util.List;

/**
 * @author tjk
 * @date 2025/1/2
 * @Description
 */
public interface DscWorkflowModelService {

    CommonResult<String> saveWorkflowModel(DscWorkflowModelDTO workflowModel);

    CommonResult<DscModel> getDscWorkflowModel(String modelId);

    CommonResult<String> deleteDscWorkflowModel(String modelId);

    CommonResult<String> updateDscWorkflowModel(DscWorkflowModelDTO workflowModel);

    CommonResult<List<DscWorkflowModelListDTO>> getDscWorkflowModelList(String ownerId);
}
