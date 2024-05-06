package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGAInvokeParams;

public interface DscGeoAnalysisTaskService {


    CommonResult<DscGeoAnalysisExecTask> submitGATask(DscGAInvokeParams params);

    CommonResult<DscGeoAnalysisExecTask> getGATask(String taskId);
}
