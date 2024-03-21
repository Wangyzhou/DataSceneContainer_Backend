package nnu.wyz.systemMS.controller;

import io.swagger.annotations.Api;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.service.DscShapeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/8 10:39
 */
@RestController
@RequestMapping(value = "/dsc-shp")
@Api(value = "DscShapefileController", tags = "空间矢量接口")
public class DscShapefileController {

    @Autowired
    private DscShapeFileService dscShapefileService;

    @GetMapping(value = "/getFields/{ptName}")
    public CommonResult<List<String>> getFields(@PathVariable String ptName) {
        return dscShapefileService.getFields(ptName);
    }

    @GetMapping(value = "/getNumericFields/{ptName}")
    public CommonResult<List<String>> getFieldsWithCheck(@PathVariable String ptName) {
        return dscShapefileService.getNumericFields(ptName);
    }

    @GetMapping(value = "/getUniqueValues/{ptName}/{field}/{method}")
    public CommonResult<List<Object>> getUniqueValues(@PathVariable String ptName, @PathVariable String field, @PathVariable String method) {
        return dscShapefileService.getUniqueValues(ptName, field, method);
    }

    @GetMapping(value = "/getFeatureCount/{tableName}")
    public CommonResult<Long> getFeatureCount(@PathVariable String tableName) {
        return dscShapefileService.getFeatureCount(tableName);
    }

    @GetMapping(value = "/getShpAttrInfoFromPG/{tableName}")
    public CommonResult<List<Map<String, Object>>> getShpAttrInfoFromPG(@PathVariable String tableName) {
        return dscShapefileService.getShpAttrInfoFromPG(tableName);
    }

    @GetMapping(value = "/getCenterAndAttrByFields/{tableName}/{fields}")
    public CommonResult<List<Map<String, Object>>> getCenterAndAttrByFields(@PathVariable String tableName,
                                                                            @PathVariable String[] fields) {
        return dscShapefileService.getCenterAndAttrByFields(tableName, fields);
    }
}
