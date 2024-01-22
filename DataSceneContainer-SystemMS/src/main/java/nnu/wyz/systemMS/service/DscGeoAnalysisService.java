package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import nnu.wyz.systemMS.model.entity.DscGeoToolExecTask;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGAInvokeParams;

public interface DscGeoAnalysisService {


    CommonResult<DscGeoAnalysisExecTask> submitGATask(DscGAInvokeParams params);

    CommonResult<DscGeoAnalysisExecTask> getGATask(String taskId);
}
