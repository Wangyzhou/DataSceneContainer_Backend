package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
import nnu.wyz.systemMS.service.DscGeoAnalysisToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/11 16:48
 */
@RestController
@RequestMapping(value = "/dsc-gatool")
public class DscGeoAnalysisToolController {

    @Autowired
    private DscGeoAnalysisToolService dscGeoAnalysisToolService;

    @GetMapping(value = "/getGATool/{toolId}")
    public CommonResult<DscGeoAnalysisTool> getGATool(@PathVariable("toolId") String toolId) {
        return dscGeoAnalysisToolService.getGeoAnalysisTool(toolId);
    }

}
