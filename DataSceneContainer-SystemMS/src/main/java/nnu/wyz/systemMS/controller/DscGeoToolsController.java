package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.entity.DscGeoTools;
import nnu.wyz.systemMS.service.DscGeoToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/5 9:40
 */
@RestController
@RequestMapping(value = "/dsc-geotools")
public class DscGeoToolsController {

    @Autowired
    private DscGeoToolsService dscGeoToolsService;

    @GetMapping(value = "/getGeoTool/{geoToolId}")
    public CommonResult<DscGeoTools> getGeoTool(@PathVariable("geoToolId") String geoToolId) {
        return dscGeoToolsService.getGeoToolInfoById(geoToolId);
    }
}
