package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscWorkflowModelDTO;
import nnu.wyz.systemMS.model.dto.DscWorkflowModelListDTO;
import nnu.wyz.systemMS.model.entity.DscModel;
import nnu.wyz.systemMS.service.DscWorkflowModelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author tjk
 * @date 2024/12/21
 * @Description
 */
@RestController
@RequestMapping("dsc-workflow")
public class DscWorkflowController {

    private final DscWorkflowModelService dscWorkflowModelService;
    @Autowired
    public DscWorkflowController(DscWorkflowModelService dscWorkflowModelService) {
        this.dscWorkflowModelService = dscWorkflowModelService;
    }


    @PostMapping("/saveWorkflowModel/{userId}")
    public CommonResult<String> saveWorkflowModel(@RequestBody DscWorkflowModelDTO workflowModelDTO,@PathVariable String userId) {
        return dscWorkflowModelService.saveWorkflowModel(workflowModelDTO , userId);
    }

    @GetMapping("/getWorkflowModel/{id}")
    public CommonResult<DscModel> getWorkflowModel(@PathVariable String id) {
        return dscWorkflowModelService.getDscWorkflowModel(id);
    }

    @DeleteMapping("/deleteWorkflowModel/{id}")
    public CommonResult<String> deleteWorkflowModel(@PathVariable String id) {
        return dscWorkflowModelService.deleteDscWorkflowModel(id);
    }

    @PutMapping("/updateWorkflowModel")
    public CommonResult<String> updateWorkflowModel(@RequestBody DscWorkflowModelDTO workflowModelDTO) {
        return dscWorkflowModelService.updateDscWorkflowModel(workflowModelDTO);
    }

    @GetMapping("/getWorkflowModelList/{id}")
    public CommonResult<List<JSONObject>> getWorkflowModelList(@PathVariable String id) {
        return dscWorkflowModelService.getDscWorkflowModelList(id);
    }


}
