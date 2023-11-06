package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.SaveGDVSceneDTO;
import nnu.wyz.systemMS.model.entity.DscGDVSceneConfig;
import nnu.wyz.systemMS.model.entity.DscScene;

public interface DscGDVSceneService {

    CommonResult<DscScene> saveGDVScene(SaveGDVSceneDTO saveGDVSceneDTO);

    DscGDVSceneConfig getGDVSceneConfig(String sceneId);
}
