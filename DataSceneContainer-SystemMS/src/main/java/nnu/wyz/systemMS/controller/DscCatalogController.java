package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;
import nnu.wyz.systemMS.service.DscCatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 10:40
 */
@RestController
@RequestMapping(value = "/dsc-catalog")
@Api(value = "DscCatalogController", tags = "目录接口")
public class DscCatalogController {

    @Autowired
    private DscCatalogService catalogService;

    @ApiOperation(value = "创建目录")
    @PostMapping
    public CommonResult<String> create(@RequestBody CreateCatalogDTO createCatalogDTO) {
        return catalogService.create(createCatalogDTO);
    }

    @ApiOperation(value = "获取目录孩子列表")
    @GetMapping("/getChildren")
    public CommonResult<List<CatalogChildrenDTO>> getChildren(@RequestParam("catalogId") String catalogId,
                                                              @RequestParam("userId") String userId) {
        return catalogService.getChildren(catalogId, userId);
    }

    @ApiOperation(value = "删除目录(同时删除目录中所有孩子节点)")
    @DeleteMapping("/delete/{catalogId}")
    public CommonResult<String> delete(@PathVariable("catalogId") String catalogId) {
        return catalogService.delete(catalogId);
    }

    @ApiOperation(value = "获取文件夹树")
    @GetMapping(value = "/getOnlyCatalogTree/{rootCatalog}")
    public CommonResult<List<JSONObject>> getOnlyCatalogTree(@PathVariable("rootCatalog")String rootCatalog) {
        return catalogService.getOnlyCatalogTree(rootCatalog);
    }
}
