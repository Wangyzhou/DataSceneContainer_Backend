package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CreatePlottingSceneDTO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.DscPlottingScene;
import nnu.wyz.systemMS.model.entity.PageInfo;

/**
 * @author tjk
 * @date 2025/8/1
 * @Description
 */
public interface DscPlottingSceneService {

    CommonResult<DscPlottingScene> createScene(CreatePlottingSceneDTO dto);

    public CommonResult<DscPlottingScene> updateScene(String sceneId, CreatePlottingSceneDTO dto);


    CommonResult<String> deleteScene(String userId, String sceneId);

    CommonResult<PageInfo<DscPlottingScene>> getSceneList(PageableDTO pageableDTO);

}
