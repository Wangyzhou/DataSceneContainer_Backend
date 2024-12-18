package nnu.wyz.systemMS.controller;

import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

/**
 * @description:公共资源的相关接口
 * @author: yzwang
 * @time: 2024/3/22 14:03
 */

@RestController
@RequestMapping(value = "/dsc-public-resource")
public class DscPublicResourceController {

    @Autowired
    DscFileService dscFileService;

    @Autowired
    DscSceneService dscSceneService;

    @Autowired
    DscPublicResourceService dscPublicResourceService;

    @Autowired
    DscVectorSService dscVectorSService;

    @Autowired
    DscRasterSService dscRasterSService;

    /**
     * @description:文件资源相关接口
     * @prefix: /file
     */

    @ApiOperation(value = "文件上传(创建文件记录、开通用户权限)")
    @PostMapping("/file")
    public CommonResult<String> upload(@RequestBody UploadFileDTO uploadFileDTO) {
        if (uploadFileDTO.getCatalogId() != null) uploadFileDTO.setCatalogId(null);
        return dscFileService.create(uploadFileDTO, true,true);
    }

    @ApiOperation(value = "文件删除")
    @DeleteMapping(value = "/file/{fileId}")
    public CommonResult<String> delete(@PathVariable("fileId") String fileId) {
        return dscPublicResourceService.deleteFile(fileId);
    }

    @ApiOperation(value = "分页获取公共文件数据列表")
    @GetMapping(value = "/file/getFileList/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<DscPublicFile>> getFileList(@RequestParam("keyword") String keyword,
                                                             @PathVariable("pageSize") Integer pageSize,
                                                             @PathVariable("pageIndex") Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO(null, keyword, pageIndex, pageSize);
        return dscPublicResourceService.getFileList(pageableDTO);
    }


    /**
     * @description:服务资源相关接口
     * @prefix: /service
     */
    @ApiOperation(value = "矢量发布：geojson")
    @PostMapping(value = "/service/publishGeoJSON2VectorS")
    public CommonResult<String> publishGeoJSON2VectorS(@RequestBody PublishGeoJSONDTO publishGeoJSONDTO) {
        return dscVectorSService.publishGeoJSON2VectorS(publishGeoJSONDTO, true);
    }

    @ApiOperation(value = "栅格发布：tif")
    @PostMapping(value = "/service/publishTiff2ImgRasterS")
    public CommonResult<String> publishTiff2ImgRasterS(@RequestBody PublishTiff2ImageDTO publishTiff2ImageDTO) {
        return dscRasterSService.publishTiff2RasterS(publishTiff2ImageDTO, true);
    }

    @ApiOperation(value = "分页获取公共矢量服务资源列表")
    @GetMapping(value = "/service/getVecServiceList/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<DscVectorServiceInfo>> getVecServiceList(@RequestParam String keyword,
                                                                          @PathVariable Integer pageSize,
                                                                          @PathVariable Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO(null, keyword, pageIndex, pageSize);
        return dscVectorSService.getVectorServiceList(pageableDTO, true);
    }

    @ApiOperation(value = "分页获取公共栅格服务资源列表")
    @GetMapping(value = "/service/getRasServiceList/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<DscRasterService>> getRasServiceList(@RequestParam String keyword,
                                                                      @PathVariable Integer pageSize,
                                                                      @PathVariable Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO(null, keyword, pageIndex, pageSize);
        return dscRasterSService.getRasterServiceList(pageableDTO, true);
    }

    @ApiOperation(value = "删除公共服务资源")
    @DeleteMapping(value = "/service/{serviceId}")
    public CommonResult<String> deleteService(@PathVariable("serviceId") String serviceId) {
        return dscPublicResourceService.deleteService(serviceId);
    }

    /**
     * @description:场景资源相关接口
     * @prefix: /scene
     */

    @ApiOperation(value = "分页获取公共场景资源列表")
    @GetMapping(value = "/scene/getSceneList/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<DscScene>> getSceneList(@RequestParam String keyword,
                                                         @PathVariable Integer pageSize,
                                                         @PathVariable Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO(null, keyword, pageIndex, pageSize);
        return dscSceneService.getSceneList(pageableDTO, true);
    }

    @ApiOperation(value = "删除公共场景资源")
    @DeleteMapping(value = "/scene/{userId}/{sceneId}")
    public CommonResult<String> deleteScene(@PathVariable("userId") String userId,
                                            @PathVariable("sceneId") String sceneId) {
        // 这里需要userId是需要作为参数，但该userId只能是内部管理员账号，这里暂未做限制
        return dscSceneService.deleteScene(userId, sceneId, true);
    }

}
