package nnu.wyz.systemMS.controller;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CreateDASceneDTO;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.service.DscDASceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    public CommonResult<DscScene> create(@RequestBody CreateDASceneDTO createDASceneDTO){
        return dscDASceneService.createDAScene(createDASceneDTO);
    }

}
