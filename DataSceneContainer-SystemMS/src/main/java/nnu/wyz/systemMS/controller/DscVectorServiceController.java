package nnu.wyz.systemMS.controller;

import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.PublishGeoJSONDTO;
import nnu.wyz.systemMS.model.dto.PublishShapefileDTO;
import nnu.wyz.systemMS.model.dto.ServiceShareImportDTO;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import nnu.wyz.systemMS.model.entity.PageInfo;
import nnu.wyz.systemMS.service.DscVectorSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/11 16:07
 */
@RestController
@RequestMapping(value = "/dsc-vector-service")
public class DscVectorServiceController {
    @Autowired
    private DscVectorSService dscVectorSService;

    @PostMapping(value = "/publishShp2VectorS")
    public CommonResult<String> publishShp2VectorS(@RequestBody PublishShapefileDTO publishShapefileDTO) {
        return dscVectorSService.publishShp2VectorS(publishShapefileDTO);
    }

    @PostMapping(value = "/publishGeoJSON2VectorS")
    public CommonResult<String> publishGeoJSON2VectorS(@RequestBody PublishGeoJSONDTO publishGeoJSONDTO) {
        return dscVectorSService.publishGeoJSON2VectorS(publishGeoJSONDTO, false);
    }

    @GetMapping(value = "/getMvt/{tableName}/{zoom}/{x}/{y}.pbf")
    public void getMvt(@PathVariable int zoom, @PathVariable int x, @PathVariable int y, @PathVariable String tableName, HttpServletResponse response) {
        dscVectorSService.getMvt(zoom, x, y, tableName, response);
    }

    @GetMapping(value = "/getVectorSList/{userId}/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<DscVectorServiceInfo>> getVectorSList(@PathVariable String userId,
                                                                       @RequestParam String keyword,
                                                                       @PathVariable Integer pageSize,
                                                                       @PathVariable Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO(userId, keyword, pageIndex, pageSize);
        return dscVectorSService.getVectorServiceList(pageableDTO, false);
    }

    @DeleteMapping(value = "/delete/{userId}/{vectorSId}")
    public CommonResult<String> deleteVectorS(@PathVariable String userId, @PathVariable String vectorSId) {
        return dscVectorSService.deleteVectorService(userId, vectorSId);
    }

    @GetMapping(value = "/getVectorSListByFileId/{fileId}")
    public CommonResult<List<DscVectorServiceInfo>> getVectorSListByFileId(@PathVariable String fileId) {
        return dscVectorSService.getVectorServiceListByFileId(fileId);
    }

    @ApiOperation(value = "矢量服务导入")
    @PostMapping(value = "/import")
    public CommonResult<String> importVectorS(@RequestBody ServiceShareImportDTO serviceShareImportDTO) {
        return dscVectorSService.importVectorS(serviceShareImportDTO);
    }
}
