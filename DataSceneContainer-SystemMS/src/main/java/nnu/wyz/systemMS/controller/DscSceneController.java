package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.PublishScene2PublicDTO;
import nnu.wyz.systemMS.model.dto.SceneShareImportDTO;
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

    @GetMapping("/getList/{userId}/{pageSize}/{pageIndex}")
    public CommonResult<PageInfo<DscScene>> getSceneList(@PathVariable("userId") String userId,
                                                         @RequestParam("keyword") String keyword,
                                                         @PathVariable("pageSize") Integer pageSize,
                                                         @PathVariable("pageIndex") Integer pageIndex) {
        PageableDTO pageableDTO = new PageableDTO(userId, keyword, pageIndex, pageSize);
        return dscSceneService.getSceneList(pageableDTO, false);
    }

    @DeleteMapping("/delete/{userId}/{sceneId}")
    public CommonResult<String> deleteScene(@PathVariable("userId") String userId, @PathVariable("sceneId") String sceneId) {
        return dscSceneService.deleteScene(userId, sceneId, false);
    }

    @GetMapping("/getSceneConfig/{sceneType}/{sceneId}")
    public CommonResult<JSONObject> getSceneConfig(@PathVariable("sceneType") String sceneType, @PathVariable("sceneId") String sceneId) {
        return dscSceneService.getSceneConfig(sceneType, sceneId);
    }

    /**
     * @param publishScene2PublicDTO
     * @return
     * @Description: 管理员发布场景到公共场景（即公共场景的创建。内部接口）
     */
    @PostMapping("/publish2Public")
    public CommonResult<String> publish2Public(@RequestBody PublishScene2PublicDTO publishScene2PublicDTO) {
        return dscSceneService.publish2Public(publishScene2PublicDTO);
    }

    @ApiOperation(value = "导入公共场景资源")
    @PostMapping(value = "/import")
    public CommonResult<String> importScene(@RequestBody SceneShareImportDTO sceneShareImportDTO) {
        return dscSceneService.importScene(sceneShareImportDTO);
    }
}
