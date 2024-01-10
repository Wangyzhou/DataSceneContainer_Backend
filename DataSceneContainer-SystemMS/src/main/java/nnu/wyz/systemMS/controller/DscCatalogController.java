package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.PageInfo;
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
    @GetMapping("/getChildren/{catalogId}")
    public CommonResult<List<CatalogChildrenDTO>> getChildren(@PathVariable("catalogId") String catalogId) {
        return catalogService.getChildren(catalogId);
    }

    @ApiOperation(value = "分页获取目录节点列表")
    @GetMapping("/getChildrenByPageable/{catalogId}/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<CatalogChildrenDTO>> getChildrenByPageable(@PathVariable("catalogId") String catalogId,
                                                                            @PathVariable("pageSize") Integer pageSize,
                                                                            @PathVariable("pageIndex") Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO();
        pageableDTO.setCriteria(catalogId);
        pageableDTO.setPageIndex(pageIndex);
        pageableDTO.setPageSize(pageSize);
        return catalogService.getChildrenByPageable(pageableDTO);
    }

    @ApiOperation(value = "删除目录(同时删除目录中所有孩子节点)")
    @DeleteMapping("/delete/{catalogId}")
    public CommonResult<String> delete(@PathVariable("catalogId") String catalogId) {
        return catalogService.delete(catalogId);
    }

    @ApiOperation(value = "获取某个目录下的文件夹树")
    @GetMapping(value = "/getOnlyCatalogTree/{rootCatalog}")
    public CommonResult<List<JSONObject>> getOnlyCatalogTree(@PathVariable("rootCatalog") String rootCatalog) {
        return catalogService.getOnlyCatalogTree(rootCatalog);
    }

    @ApiOperation(value = "获取某个目录下所有的目录（包括文件）项树")
    @GetMapping(value = "/getCatalogChildrenTree/{rootCatalog}")
    public CommonResult<List<JSONObject>> getCatalogChildrenTree(@PathVariable("rootCatalog") String rootCatalog) {
        return catalogService.getCatalogChildrenTree(rootCatalog);
    }

    @ApiOperation(value = "获取当前文件夹路径（前端）")
    @GetMapping(value = "/pwd/{catalogId}")
    public CommonResult<String> pwd(@PathVariable("catalogId") String catalogId) {
        return catalogService.pwd(catalogId);
    }

}
