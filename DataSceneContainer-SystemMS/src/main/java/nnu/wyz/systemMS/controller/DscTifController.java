package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.FalseColorCompositeDTO;
import nnu.wyz.systemMS.model.dto.RenderTifDTO;
import nnu.wyz.systemMS.service.DscTifService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/2/22 15:36
 */
@RestController
@RequestMapping(value = "/dsc-tif")
public class DscTifController {

    @Autowired
    private DscTifService dscTifService;

    @GetMapping(value = "/getBandCount/{userId}/{rasterSId}")
    public CommonResult<Integer> getBandCount(@PathVariable String userId, @PathVariable String rasterSId) {
        return dscTifService.getBandCount(userId, rasterSId);
    }

    @PostMapping(value = "/changeColorMap")
    public CommonResult<String> changeColorMap(@RequestBody RenderTifDTO renderTifDTO) {
        return dscTifService.changeColorMap(renderTifDTO);
    }

    @PostMapping(value = "/falseColorComposite")
    public CommonResult<String> falseColorComposite(@RequestBody FalseColorCompositeDTO falseColorCompositeDTO) {
        return dscTifService.falseColorComposite(falseColorCompositeDTO);
    }
}
