package nnu.wyz.fileMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.fileMS.model.dto.PublishShapefileDTO;
import nnu.wyz.fileMS.model.dto.UnzipFileDTO;
import nnu.wyz.fileMS.service.DscFileEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/6 10:09
 */
@RestController
@RequestMapping("dsc-file-engine")
public class DscFileEngineController {

    @Autowired
    private DscFileEngineService dscFileEngineService;

    @PostMapping("/unzip")
    public CommonResult<String> unzip(@RequestBody UnzipFileDTO unzipFileDTO) {
        return dscFileEngineService.unzip(unzipFileDTO);
    }

    @PostMapping("/publishMvt")
    public CommonResult<String> publishMvt(@RequestBody PublishShapefileDTO publishShapefileDTO) {
        return dscFileEngineService.publishShapefile(publishShapefileDTO);
    }
}
