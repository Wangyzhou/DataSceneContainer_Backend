package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.Page;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.MongoTransactional;
import nnu.wyz.systemMS.dao.DscGDVSceneConfigDAO;
import nnu.wyz.systemMS.dao.DscSceneDAO;
import nnu.wyz.systemMS.dao.DscUserSceneDAO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.DscGDVSceneConfig;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.DscUserScene;
import nnu.wyz.systemMS.model.entity.PageInfo;
import nnu.wyz.systemMS.service.DscGDVSceneService;
import nnu.wyz.systemMS.service.DscSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/15 11:07
 */

@Service
public class DscSceneServiceIml implements DscSceneService {

    @Autowired
    private DscUserSceneDAO dscUserSceneDAO;

    @Autowired
    private DscSceneDAO dscSceneDAO;

    @Autowired
    private DscGDVSceneConfigDAO dscGDVSceneConfigDAO;

    @Autowired
    private DscGDVSceneService dscGDVSceneService;
    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Override
    public CommonResult<PageInfo<DscScene>> getSceneList(PageableDTO pageableDTO) {
        String userId = pageableDTO.getCriteria();
        Integer pageIndex = pageableDTO.getPageIndex();
        Integer pageSize = pageableDTO.getPageSize();
        List<DscScene> sceneListNoLimit = dscUserSceneDAO.findAllByUserId(userId)
                .stream()
                .map(DscUserScene::getSceneId)
                .map(dscSceneDAO::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(DscScene::getUpdatedTime).reversed()).collect(Collectors.toList());
        List<DscScene> sceneList = sceneListNoLimit
                .stream()
                .skip((long) (pageIndex - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        PageInfo<DscScene> dscScenePageInfo = new PageInfo<>(sceneList, sceneListNoLimit.size(), sceneListNoLimit.size() / pageSize + 1);
        return CommonResult.success(dscScenePageInfo, "获取场景列表成功！");
    }

    @Override
    public CommonResult<String> deleteScene(String userId, String sceneId) {
        DscUserScene byUserIdAndSceneId = dscUserSceneDAO.findByUserIdAndSceneId(userId, sceneId);
        if (Objects.isNull(byUserIdAndSceneId)) {
            return CommonResult.failed("场景不存在！");
        }
        Optional<DscScene> byId = dscSceneDAO.findById(sceneId);
        DscScene dscScene = byId.get();
        String thumbnail = dscScene.getThumbnail();
        int oKBegin = thumbnail.indexOf(userId);
        String objectKey = thumbnail.substring(oKBegin);
        DscGDVSceneConfig dscGDVSceneConfig = dscGDVSceneConfigDAO.findBySceneId(sceneId);
        //删除缩略图
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(minioConfig.getSceneThumbnailsBucket(), objectKey);
        amazonS3.deleteObject(deleteObjectRequest);
        //删除scene和config
        dscUserSceneDAO.delete(byUserIdAndSceneId);
        dscSceneDAO.delete(dscScene);
        dscGDVSceneConfigDAO.delete(dscGDVSceneConfig);
        return CommonResult.success("删除场景成功！");
    }

    @Override
    public CommonResult<JSONObject> getSceneConfig(String sceneType, String sceneId) {
        JSONObject sceneConfig;
        switch (sceneType) {
            case "GDV":
                DscGDVSceneConfig gdvSceneConfig = dscGDVSceneService.getGDVSceneConfig(sceneId);
                sceneConfig = BeanUtil.toBean(gdvSceneConfig, JSONObject.class);
                break;
            case "GDA":
                sceneConfig = new JSONObject();
                break;
            default:
                sceneConfig = new JSONObject();
                break;
        }
        return CommonResult.success(sceneConfig, "获取场景配置成功！");
    }
}
