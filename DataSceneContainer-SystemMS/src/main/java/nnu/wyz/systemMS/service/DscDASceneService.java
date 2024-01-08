package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CreateDASceneDTO;
import nnu.wyz.systemMS.model.entity.DscScene;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/1/6 15:48
 */
public interface DscDASceneService {

    CommonResult<DscScene> createDAScene(CreateDASceneDTO createDASceneDTO);

}
