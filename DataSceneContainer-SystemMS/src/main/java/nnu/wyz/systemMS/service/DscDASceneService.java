package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CreateDASceneDTO;
import nnu.wyz.systemMS.model.entity.DscDASceneConfig;
import nnu.wyz.systemMS.model.entity.DscScene;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/1/6 15:48
 */
public interface DscDASceneService {

    CommonResult<DscScene> createDAScene(CreateDASceneDTO createDASceneDTO);

    DscDASceneConfig getDASceneConfig(String sceneId);

    CommonResult<List<JSONObject>> addData2Scene(String sceneId, String fileId, String type);

}
