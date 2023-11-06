package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.MapParamsDTO;
import nnu.wyz.systemMS.model.dto.SaveGDVSceneDTO;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.GDVSceneSource;
import nnu.wyz.systemMS.service.DscGDVSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    @PutMapping
    public CommonResult<DscScene> save(@RequestParam("userId") String userId,
                                       @RequestParam("sceneId") String sceneId,
                                       @RequestParam("name") String name,
                                       @RequestParam("thumbnail") MultipartFile thumbnail,
                                       @RequestParam("sources") String sources,
                                       @RequestParam("layers") String layers,
                                       @RequestParam("pos") String pos,
                                       @RequestParam("mapParams") String mapParams) {
        List<GDVSceneSource> sceneSources = JSONObject.parseArray(sources, GDVSceneSource.class);
        List<JSONObject> sceneLayers = JSONObject.parseArray(layers, JSONObject.class);
        JSONObject scenePosition = JSONObject.parseObject(pos, JSONObject.class);
        MapParamsDTO sceneMapParams = JSONObject.parseObject(mapParams, MapParamsDTO.class);
        SaveGDVSceneDTO saveGDVSceneDTO = new SaveGDVSceneDTO(userId, sceneId, name, thumbnail, sceneSources, sceneLayers, scenePosition, sceneMapParams);
        return dscGDVSceneService.saveGDVScene(saveGDVSceneDTO);
    }


}
