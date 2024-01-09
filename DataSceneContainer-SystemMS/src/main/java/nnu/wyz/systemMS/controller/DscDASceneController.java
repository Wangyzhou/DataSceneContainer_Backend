package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CreateDASceneDTO;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.service.DscDASceneService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/9 10:52
 */
@RestController
@RequestMapping(value = "/dsc-da-scene")

public class DscDASceneController {

    @Autowired
    private DscDASceneService dscDASceneService;

    @PostMapping
    public CommonResult<DscScene> create(@RequestBody CreateDASceneDTO createDASceneDTO) {
        return dscDASceneService.createDAScene(createDASceneDTO);
    }

    @PostMapping(value = "/addData2Scene")
    public CommonResult<List<JSONObject>> addData2Scene(@RequestBody Map<String, String> params) {
        return dscDASceneService.addData2Scene(params.get("sceneId"), params.get("fileId"), params.get("type"));
    }

}
