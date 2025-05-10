package nnu.wyz.systemMS.service.DscCode;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGAInvokeParams;
import nnu.wyz.systemMS.model.dto.DscCodeModelDTO;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModel;
import nnu.wyz.systemMS.model.param.code.CodeParams;

import java.util.List;

public interface DscCodeModelService {
    CommonResult<String> encapsulate(DscCodeModelDTO dscCodeModelDTO);
    CommonResult<DscCodeModel> getToolInfo(String toolId);
    CommonResult<String> delete(String toolId);
    CommonResult<?> getExecuteResult(CodeParams params);
}
