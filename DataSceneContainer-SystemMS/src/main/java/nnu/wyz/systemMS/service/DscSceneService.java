package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.entity.DscScene;

import java.util.List;

public interface DscSceneService {

    CommonResult<List<DscScene>> getSceneList(String userId);

    CommonResult<String> deleteScene(String userId, String sceneId);

}
