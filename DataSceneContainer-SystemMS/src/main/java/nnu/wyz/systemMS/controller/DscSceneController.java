package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.PageInfo;
import nnu.wyz.systemMS.service.DscSceneService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/getList/{userId}/{keyword}/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<DscScene>> getSceneList(@PathVariable("userId") String userId,
                                                         @PathVariable("keyword") String keyword,
                                                         @PathVariable("pageSize") Integer pageSize,
                                                         @PathVariable("pageIndex") Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO(userId, keyword, pageIndex, pageSize);
        return dscSceneService.getSceneList(pageableDTO);
    }

    @DeleteMapping("/delete/{userId}/{sceneId}")
    public CommonResult<String> deleteScene(@PathVariable("userId") String userId, @PathVariable("sceneId") String sceneId) {
        return dscSceneService.deleteScene(userId, sceneId);
    }

    @GetMapping("/getSceneConfig/{sceneType}/{sceneId}")
    public CommonResult<JSONObject> getSceneConfig(@PathVariable("sceneType") String sceneType, @PathVariable("sceneId") String sceneId) {
        return dscSceneService.getSceneConfig(sceneType, sceneId);
    }
}
