package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.PageInfo;

import java.util.List;

public interface DscSceneService {

    CommonResult<PageInfo<DscScene>> getSceneList(PageableDTO pageableDTO);

    CommonResult<String> deleteScene(String userId, String sceneId);

    CommonResult<JSONObject> getSceneConfig(String sceneType, String sceneId);
}
