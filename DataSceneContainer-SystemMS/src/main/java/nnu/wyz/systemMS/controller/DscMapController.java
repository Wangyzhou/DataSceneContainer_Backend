package nnu.wyz.systemMS.controller;

import io.swagger.annotations.Api;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.DscMap;
import nnu.wyz.systemMS.model.entity.PageInfo;
import nnu.wyz.systemMS.service.DscMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(value = "DscMapController", tags = "战例底图接口")
@RequestMapping("/dsc-map")
public class DscMapController {

    @Autowired
    private DscMapService dscMapService;

    @GetMapping("/getList/{userId}/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<DscMap>> getMapList(@PathVariable("userId") String userId,
                                                     @RequestParam("keyword") String keyword,
                                                     @PathVariable("pageSize") Integer pageSize,
                                                     @PathVariable("pageIndex") Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO(userId, keyword, pageIndex, pageSize);
        return dscMapService.getMapList(pageableDTO);
    }

    @DeleteMapping("/delete/{userId}/{mapId}")
    public CommonResult<String> deleteMap(@PathVariable("mapId") String mapId, @PathVariable("userId") String userId) {
        return dscMapService.deleteMap(mapId, userId);
    }
}
