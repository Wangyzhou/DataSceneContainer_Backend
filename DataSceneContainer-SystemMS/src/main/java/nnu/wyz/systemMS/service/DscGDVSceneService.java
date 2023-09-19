package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CreateGDVSceneDTO;

public interface DscGDVSceneService {

    CommonResult<String> createGDVScene(CreateGDVSceneDTO createGDVSceneDTO);

}
