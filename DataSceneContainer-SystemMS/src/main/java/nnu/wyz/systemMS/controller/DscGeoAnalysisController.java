package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisExecTask;
import nnu.wyz.systemMS.model.entity.DscGeoToolExecTask;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGAInvokeParams;
import nnu.wyz.systemMS.service.DscGeoAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 10:03
 */
@RestController
@RequestMapping(value = "/dsc-geoAnalysis")
public class DscGeoAnalysisController {

    @Autowired
    private DscGeoAnalysisService dscGeoAnalysisService;

    @PostMapping(value = "/submitGATask")
    CommonResult<DscGeoAnalysisExecTask> submitGATask(@RequestBody DscGAInvokeParams params) {
        return dscGeoAnalysisService.submitGATask(params);
    }

    @GetMapping(value = "/getGATask/{taskId}")
    public CommonResult<DscGeoToolExecTask> getTask(@PathVariable("taskId") String taskId) {
        return dscGeoAnalysisService.getGATask(taskId);
    }

}
