package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.SaveGDVSceneDTO;
import nnu.wyz.systemMS.model.entity.DscGDVSceneConfig;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.GDVSceneSource;
import nnu.wyz.systemMS.model.entity.ServiceRefs;

import java.util.List;

public interface DscGDVSceneService {

    CommonResult<DscScene> saveGDVScene(SaveGDVSceneDTO saveGDVSceneDTO);

    DscGDVSceneConfig getGDVSceneConfig(String sceneId);

    ServiceRefs getSourcesToAddRef(List<GDVSceneSource> lastSources, List<GDVSceneSource> currentSources);

    ServiceRefs getSourcesToMinusRef(List<GDVSceneSource> lastSources, List<GDVSceneSource> currentSources, boolean skipTif);

}
