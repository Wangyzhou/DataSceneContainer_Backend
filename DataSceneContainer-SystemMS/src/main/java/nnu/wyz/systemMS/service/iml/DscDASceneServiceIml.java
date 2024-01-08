package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.sun.org.apache.xml.internal.resolver.Catalog;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscDASceneConfigDAO;
import nnu.wyz.systemMS.dao.DscSceneDAO;
import nnu.wyz.systemMS.dao.DscUserSceneDAO;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;
import nnu.wyz.systemMS.model.dto.CreateDASceneDTO;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.model.entity.DscDASceneConfig;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.DscUserScene;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscDASceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/1/6 15:58
 */
@Service
public class DscDASceneServiceIml implements DscDASceneService {

    @Autowired
    private DscCatalogDAO dscCatalogDAO;

    @Autowired
    private DscCatalogService dscCatalogService;

    @Autowired
    private DscUserSceneDAO dscUserSceneDAO;

    @Autowired
    private DscSceneDAO dscSceneDAO;

    @Autowired
    private DscDASceneConfigDAO dscDASceneConfigDAO;

    private final static String SCENE_TYPE = "DAS";

    @Override
    public CommonResult<DscScene> createDAScene(CreateDASceneDTO createDASceneDTO) {

        //创建场景
        String sceneId = IdUtil.randomUUID();
        DscUserScene isExist = dscUserSceneDAO.findByUserIdAndSceneName(createDASceneDTO.getUserId(), createDASceneDTO.getName());
        if (!Objects.isNull(isExist) && !isExist.getSceneId().equals(sceneId)) {
            return CommonResult.failed("与现有场景名重复，请修改名称后重新创建！");
        }
        String createdTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        DscScene dscScene = new DscScene(sceneId, createDASceneDTO.getName(), SCENE_TYPE, null, createDASceneDTO.getUserId(), 1L, createdTime, createdTime, false, 16);
        dscSceneDAO.save(dscScene);

        // 创建场景的数据文件夹
        DscCatalog sceneDataRootCatalog = dscCatalogDAO.findDscCatalogByUserIdAndParentAndTaskId(createDASceneDTO.getUserId(),
                dscCatalogDAO.findDscCatalogByUserIdAndParent(createDASceneDTO.getUserId(), "-1").getId(), "-1");
        CreateCatalogDTO createCatalogDTO = new CreateCatalogDTO();
        createCatalogDTO.setUserId(createDASceneDTO.getUserId());
        //场景名的唯一性，决定场景文件夹名称的唯一性
        createCatalogDTO.setCatalogName(createDASceneDTO.getName());
        createCatalogDTO.setParentCatalogId(sceneDataRootCatalog.getId());
        createCatalogDTO.setTaskId("-1");
        String newSceneCatalogId = dscCatalogService.create(createCatalogDTO).getData();

        // 创建场景相关记录
        String userSceneId = IdUtil.randomUUID();
        DscUserScene dscUserScene = new DscUserScene();
        dscUserScene.setId(userSceneId)
                .setSceneName(createDASceneDTO.getName())
                .setUserId(createDASceneDTO.getUserId())
                .setSceneId(sceneId);
        dscUserSceneDAO.save(dscUserScene);
        DscDASceneConfig dscDASceneConfig = new DscDASceneConfig();
        dscDASceneConfig.setId(IdUtil.objectId()).setSceneId(dscScene.getId());
        dscDASceneConfig.setSceneDataRootCatalogId(newSceneCatalogId);
        dscDASceneConfigDAO.insert(dscDASceneConfig);
        return CommonResult.success(dscScene, "场景创建成功！");
    }

}
