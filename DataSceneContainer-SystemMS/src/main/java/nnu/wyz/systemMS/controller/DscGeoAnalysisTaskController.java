package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGAInvokeParams;
import nnu.wyz.systemMS.service.DscGeoAnalysisTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 10:03
 */
@RestController
@RequestMapping(value = "/dsc-geoAnalysis")
public class DscGeoAnalysisTaskController {

    @Autowired
    private DscGeoAnalysisTaskService dscGeoAnalysisTaskService;

    @PostMapping(value = "/submitGATask")
    CommonResult<DscGeoAnalysisExecTask> submitGATask(@RequestBody DscGAInvokeParams params) {
        return dscGeoAnalysisTaskService.submitGATask(params);
    }

    @GetMapping(value = "/getGATask/{taskId}")
    public CommonResult<DscGeoAnalysisExecTask> getTask(@PathVariable("taskId") String taskId) {
        return dscGeoAnalysisTaskService.getGATask(taskId);
    }

}
