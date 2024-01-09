package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;
import nnu.wyz.systemMS.model.dto.CreateDASceneDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscDASceneService;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @Autowired
    private DscFileDAO dscFileDAO;

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
        DscCatalog userSceneDataRootCatalog = dscCatalogDAO.findDscCatalogByUserIdAndParentAndTaskId(createDASceneDTO.getUserId(),"-1", "-1");
        CreateCatalogDTO createCatalogDTO = new CreateCatalogDTO();
        createCatalogDTO.setUserId(createDASceneDTO.getUserId());
        //场景名的唯一性，决定场景文件夹名称的唯一性
        createCatalogDTO.setCatalogName(createDASceneDTO.getName());
        createCatalogDTO.setParentCatalogId(userSceneDataRootCatalog.getId());
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

    @Override
    public DscDASceneConfig getDASceneConfig(String sceneId) {
        return dscDASceneConfigDAO.findBySceneId(sceneId);
    }

    @Override
    public CommonResult<List<JSONObject>> addData2Scene(String sceneId, String fileId, String type) {
        DscDASceneConfig dscDASceneConfig = dscDASceneConfigDAO.findBySceneId(sceneId);
        DscCatalog sceneDataRootCatalog = dscCatalogDAO.findDscCatalogById(dscDASceneConfig.getSceneDataRootCatalogId());
        //  如果是单个文件
        if (!type.equals("folder")) {
            Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
            if (!byId.isPresent()) {
                return CommonResult.failed("文件不存在！");
            }
            DscFileInfo dscFileInfo = byId.get();
            //1、更新文件信息（文件拥有者计数+1）
            dscFileInfo.setOwnerCount(dscFileInfo.getOwnerCount() + 1);
            dscFileInfo.setUpdatedTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            dscFileDAO.save(dscFileInfo);
            //2、场景数据根目录增加孩子节点
            CatalogChildrenDTO catalogChildrenDTO = new CatalogChildrenDTO();
            catalogChildrenDTO.setId(fileId)
                    .setName(dscFileInfo.getFileName())
                    .setType(dscFileInfo.getFileSuffix())
                    .setUpdatedTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            sceneDataRootCatalog.getChildren().add(catalogChildrenDTO);
            sceneDataRootCatalog.setUpdatedTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            sceneDataRootCatalog.setTotal(sceneDataRootCatalog.getTotal() + 1);
            dscCatalogDAO.save(sceneDataRootCatalog);
        }
        return dscCatalogService.getCatalogChildrenTree(sceneDataRootCatalog.getId());
    }
}
