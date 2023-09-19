package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.SaveGDVSceneDTO;
import nnu.wyz.systemMS.model.entity.DscGDVSceneConfig;

public interface DscGDVSceneService {

    CommonResult<String> saveGDVScene(SaveGDVSceneDTO saveGDVSceneDTO);

    DscGDVSceneConfig getGDVSceneConfig(String sceneId);
}
