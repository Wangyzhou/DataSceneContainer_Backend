package nnu.wyz.systemMS.controller.DscCode;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscCodeModelDTO;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModel;
import nnu.wyz.systemMS.service.DscCode.DscCodeModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dsc-code-model")
public class DscCodeModelController {

    @Autowired
    private DscCodeModelService dscCodeModelService;
    @PostMapping("/encapsulation")
    private CommonResult<String> encapsulation(@RequestBody DscCodeModelDTO dscCodeModel){
        return dscCodeModelService.encapsulate(dscCodeModel);
    }

    @GetMapping("/getToolInfo/{toolId}")
    private CommonResult<DscCodeModel> getToolInfo(@PathVariable String toolId){
        return dscCodeModelService.getToolInfo(toolId);
    }

    @PostMapping("/delete/{toolId}")
    private CommonResult<String> delete(@PathVariable String toolId){
        return dscCodeModelService.delete(toolId);
    }
}
