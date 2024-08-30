package nnu.wyz.systemMS.controller;

import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.PageInfo;
import nnu.wyz.systemMS.service.DscRasterSService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/2 10:43
 */
@RestController
@RequestMapping(value = "/dsc-raster-service")
public class DscRasterServiceController {

    @Resource
    private DscRasterSService dscRasterSService;

    @PostMapping(value = "/publishImage2RasterS")
    public CommonResult<String> publishImage2RasterS(@RequestBody PublishImageDTO publishImageDTO) {
        return dscRasterSService.publishImage2RasterS(publishImageDTO);
    }

    @PostMapping(value = "/publishTiff2ImgRasterS")
    public CommonResult<String> publishTiff2ImgRasterS(@RequestBody PublishTiff2ImageDTO publishTiff2ImageDTO) {
        return dscRasterSService.publishTiff2RasterS(publishTiff2ImageDTO, false);
    }

    @PostMapping(value = "/publishTiff2TMS")
    public CommonResult<String> publishTiff2TMS(@RequestBody PublishTiff2TMSDTO publishTiff2TMSDTO) {
        return dscRasterSService.publishTiff2TMS(publishTiff2TMSDTO);
    }

    @GetMapping(value = "/getRasterSList/{userId}/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<DscRasterService>> getRasterSList(@PathVariable(value = "userId") String userId,
                                                                   @RequestParam(value = "keyword") String keyword,
                                                                   @PathVariable(value = "pageSize") Integer pageSize,
                                                                   @PathVariable(value = "pageIndex") Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO(userId, keyword, pageIndex, pageSize);
        return dscRasterSService.getRasterServiceList(pageableDTO, false);
    }

    @DeleteMapping(value = "/delete/{userId}/{rasterSId}")
    public CommonResult<String> deleteRasterS(@PathVariable(value = "userId") String userId,
                                              @PathVariable(value = "rasterSId") String rasterSId) {
        return dscRasterSService.deleteRasterService(userId, rasterSId);
    }

    @GetMapping(value = "/getRasterSListByFileId/{fileId}")
    public CommonResult<List<DscRasterService>> getRasterSListByFileId(@PathVariable(value = "fileId") String fileId) {
        return dscRasterSService.getRasterServiceListByFileId(fileId);
    }

    @GetMapping(value = "/getRasterTiles/{userId}/{rasterSId}/{z}/{x}/{y}.png")
    public void getRasterTiles(@PathVariable(value = "z") Integer z,
                               @PathVariable(value = "x") Integer x,
                               @PathVariable(value = "y") Integer y,
                               @PathVariable(value = "userId") String userId,
                               @PathVariable(value = "rasterSId") String rasterSId,
                               HttpServletResponse response) {
        dscRasterSService.getRasterTiles(z, x, y, userId, rasterSId, response);
    }

    /**
     * 根据场景引用添加一个栅格服务副本
     *
     * @return 子服务的url
     */
    @PostMapping(value = "/addRasterSCopy")
    public CommonResult<String> addRasterSCopy(@RequestBody GetRasterSCopyDTO getRasterSCopyDTO) {
        return dscRasterSService.addRasterSCopy(getRasterSCopyDTO);
    }

    @DeleteMapping(value = "/deleteRasterSCopy/{sceneId}/{rasterSId}")
    public CommonResult<String> deleteRasterSCopy(@PathVariable String sceneId, @PathVariable String rasterSId) {
        return dscRasterSService.deleteRasterSCopy(sceneId, rasterSId);
    }

    @ApiOperation(value = "栅格服务导入")
    @PostMapping(value = "/import")
    public CommonResult<String> importRasterS(@RequestBody ServiceShareImportDTO serviceShareImportDTO) {
        return dscRasterSService.importRasterS(serviceShareImportDTO);
    }
}
