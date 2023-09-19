package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.service.DscSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/15 11:06
 */
@RestController
@Api(value = "DscSceneController", tags = "场景接口")
@RequestMapping("/dsc-scene")
public class DscSceneController {

    @Autowired
    private DscSceneService dscSceneService;

    @GetMapping("/getList/{userId}")
    public CommonResult<List<DscScene>> getSceneList(@PathVariable("userId") String userId) {
        return dscSceneService.getSceneList(userId);
    }

    @DeleteMapping("/delete/{userId}/{sceneId}")
    public CommonResult<String> deleteScene(@PathVariable("userId") String userId,@PathVariable("sceneId") String sceneId) {
        return dscSceneService.deleteScene(userId,sceneId);
    }
}
