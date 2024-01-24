package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
import nnu.wyz.systemMS.model.dto.ConvertSgrd2GeoTIFFDTO;
import nnu.wyz.systemMS.service.DscGeoAnalysisToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(value = "/convertSgrd2Geotiff")
    public CommonResult<String> convertSgrd2Geotiff(@RequestBody ConvertSgrd2GeoTIFFDTO convertSgrd2GeoTIFFDTO) {
        return dscGeoAnalysisToolService.convertSgrd2Geotiff(convertSgrd2GeoTIFFDTO);
    }

}
