package nnu.wyz.fileMS.controller;

import io.swagger.annotations.Api;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.fileMS.service.DscShapefileService;
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
 * @time: 2023/10/8 10:39
 */
@RestController
@RequestMapping(value = "/dsc-shp")
@Api(value = "DscShapefileController",tags = "空间矢量接口")
public class DscShapefileController {

    @Autowired
    private DscShapefileService dscShapefileService;

    @GetMapping(value = "/getFields/{ptName}")
    public CommonResult<List<String>> getFields(@PathVariable String ptName) {
        return dscShapefileService.getFields(ptName);
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
}
