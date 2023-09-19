package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CreateGDVSceneDTO;
import nnu.wyz.systemMS.service.DscGDVSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/12 20:41
 */
@RestController
@RequestMapping(value = "/dsc-gdv-scene")
@Api(value = "DscGDVSceneController", tags = "地理数据可视化场景接口")
public class DscGDVSceneController {

    @Autowired
    private DscGDVSceneService dscGDVSceneService;

    @PostMapping
    public CommonResult<String> create(@RequestParam("userId") String userId,
                                       @RequestParam("name") String name,
                                       @RequestParam("thumbnail") MultipartFile thumbnail,
                                       @RequestParam("sources") String sources,
                                       @RequestParam("layers") String layers,
                                       @RequestParam("pos") String pos,
                                       @RequestParam("mapParams") String mapParams) {
        List<JSONObject> sceneSources = JSONObject.parseArray(sources, JSONObject.class);
        List<JSONObject> sceneLayers = JSONObject.parseArray(layers, JSONObject.class);
        JSONObject scenePosition = JSONObject.parseObject(pos, JSONObject.class);
        JSONObject sceneMapParams = JSONObject.parseObject(mapParams, JSONObject.class);
        CreateGDVSceneDTO createGDVSceneDTO = new CreateGDVSceneDTO(userId, name, thumbnail, sceneSources, sceneLayers, scenePosition, sceneMapParams);
        return dscGDVSceneService.createGDVScene(createGDVSceneDTO);
    }
}
