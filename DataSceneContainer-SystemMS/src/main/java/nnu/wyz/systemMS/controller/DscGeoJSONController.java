package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.service.DscGeoJSONService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/18 14:09
 */
@SuppressWarnings("rawtypes")
@RestController
@RequestMapping(value = "dsc-geojson")
public class DscGeoJSONController {

    @Autowired
    private DscGeoJSONService dscGeoJSONService;
    @GetMapping(value = "/getFields/{vectorSId}")
    public CommonResult<List<String>> getFields(@PathVariable("vectorSId") String vectorSId) {
        return dscGeoJSONService.getFields(vectorSId);
    }

    @GetMapping(value = "/getAttrs/{vectorSId}")
    public CommonResult<List<Map<String, Object>>> getAttrs(@PathVariable("vectorSId") String vectorSId) {
        return dscGeoJSONService.getAttrs(vectorSId);
    }

    @GetMapping(value = "/getUniqueValues/{vectorSId}/{field}/{method}")
    public CommonResult<List> getUniqueValues(@PathVariable("vectorSId") String vectorSId, @PathVariable String field, @PathVariable String method) {
        return dscGeoJSONService.getUniqueValues(vectorSId, field, method);
    }

    @GetMapping(value = "/getFeatureCount/{vectorSId}")
    public CommonResult<Integer> getFeatureCount(@PathVariable("vectorSId") String vectorSId) {
        return dscGeoJSONService.getFeatureCount(vectorSId);
    }
}
