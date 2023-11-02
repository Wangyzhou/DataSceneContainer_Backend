package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PublishImageDTO;
import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import nnu.wyz.systemMS.service.DscRasterSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
   private DscRasterSService dscRasterService;

   @PostMapping(value = "/publishImage2RasterS")
    public CommonResult<String> publishImage2RasterS(@RequestBody PublishImageDTO publishImageDTO) {
        return dscRasterService.publishImage2RasterS(publishImageDTO);
    }

    @GetMapping(value = "/getRasterSList/{userId}")
    public CommonResult<List<DscRasterService>> getRasterSList(@PathVariable(value = "userId") String userId) {
        return dscRasterService.getRasterServiceList(userId);
    }

    @DeleteMapping(value = "/delete/{userId}/{rasterSId}")
    public CommonResult<String> deleteRasterS(@PathVariable(value = "userId" ) String userId,
                                              @PathVariable(value = "rasterSId") String rasterSId) {
        return dscRasterService.deleteRasterService(userId, rasterSId);
    }
}
