package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.PublishScene2PublicDTO;
import nnu.wyz.systemMS.model.dto.SceneShareImportDTO;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.PageInfo;

import java.util.List;

public interface DscSceneService {

    /**
     * @param pageableDTO
     * @param isPublic:区分公共和个人
     * @return
     */
    CommonResult<PageInfo<DscScene>> getSceneList(PageableDTO pageableDTO, boolean isPublic);

    CommonResult<String> deleteScene(String userId, String sceneId, boolean isPublic);

    CommonResult<JSONObject> getSceneConfig(String sceneType, String sceneId);

    CommonResult<String> publish2Public(PublishScene2PublicDTO publishScene2PublicDTO);

    CommonResult<String> importScene(SceneShareImportDTO sceneShareImportDTO);
}
