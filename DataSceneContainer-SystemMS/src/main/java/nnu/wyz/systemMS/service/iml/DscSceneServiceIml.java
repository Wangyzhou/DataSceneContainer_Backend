package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.Page;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.MongoTransactional;
import nnu.wyz.systemMS.dao.DscDASceneConfigDAO;
import nnu.wyz.systemMS.dao.DscGDVSceneConfigDAO;
import nnu.wyz.systemMS.dao.DscSceneDAO;
import nnu.wyz.systemMS.dao.DscUserSceneDAO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.*;
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
@Slf4j
public class DscSceneServiceIml implements DscSceneService {

    @Autowired
    private DscUserSceneDAO dscUserSceneDAO;

    @Autowired
    private DscSceneDAO dscSceneDAO;

    @Autowired
    private DscGDVSceneConfigDAO dscGDVSceneConfigDAO;

    @Autowired
    private DscDASceneConfigDAO dscDASceneConfigDAO;

    @Autowired
    private DscGDVSceneService dscGDVSceneService;

    @Autowired
    private DscDASceneService dscDASceneService;

    @Autowired
    private DscCatalogService dscCatalogService;

    @Autowired
    private DscVectorSService dscVectorSService;

    @Autowired
    private DscRasterSService dscRasterSService;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Override
    public CommonResult<PageInfo<DscScene>> getSceneList(PageableDTO pageableDTO) {
        String userId = pageableDTO.getCriteria();
        String keyword = pageableDTO.getKeyword(); // 新增关键词参数
        Integer pageIndex = pageableDTO.getPageIndex();
        Integer pageSize = pageableDTO.getPageSize();
        List<DscScene> sceneListNoLimit = dscUserSceneDAO.findAllByUserId(userId)
                .stream()
                .map(DscUserScene::getSceneId)
                .map(dscSceneDAO::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(info -> info.getName().contains(keyword)) // 根据关键词进行模糊匹配
                .sorted(Comparator.comparing(DscScene::getUpdatedTime).reversed()).collect(Collectors.toList());
        List<DscScene> sceneList = sceneListNoLimit
                .stream()
                .skip((long) (pageIndex - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        PageInfo<DscScene> dscScenePageInfo = new PageInfo<>(sceneList, sceneListNoLimit.size(), (int) Math.ceil((double) sceneListNoLimit.size() / pageSize));
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
        //如果缩略图存在
        if (thumbnail != null && !thumbnail.isEmpty()) {
            int oKBegin = thumbnail.indexOf(userId);
            String objectKey = thumbnail.substring(oKBegin);
            //删除缩略图
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(minioConfig.getSceneThumbnailsBucket(), objectKey);
            amazonS3.deleteObject(deleteObjectRequest);
        }
        //删除scene和config
        dscUserSceneDAO.delete(byUserIdAndSceneId);
        dscSceneDAO.delete(dscScene);
        String sceneType = dscScene.getType();
        if (sceneType.equals("GDV")) {
            DscGDVSceneConfig dscGDVSceneConfig = dscGDVSceneConfigDAO.findBySceneId(sceneId);
            // 删除除tif栅格外的服务引用，ownerCount-1
            //TODO:暂时只在删除制图场景时进行该操作，等分析场景保存功能完善后，增加该操作
            ServiceRefs minusRefs = dscGDVSceneService.getSourcesToMinusRef(dscGDVSceneConfig.getSources(), new ArrayList<>());
            log.info("删除场景引用的矢量服务：" + minusRefs.getVectorRefs());
            log.info("删除场景引用的非tif栅格服务：" + minusRefs.getRasterRefs());
            if (!minusRefs.getVectorRefs().isEmpty())
                dscVectorSService.updateOwnerCount(minusRefs.getVectorRefs(), false);
            if (!minusRefs.getRasterRefs().isEmpty())
                // 更新非tif源的引用
                dscRasterSService.updateOwnerCount(minusRefs.getRasterRefs(), false);
            // 删除场景所有tif源副本
            List<String> tifSourceIds = dscGDVSceneConfig.getSources()
                    .stream()
                    .filter(gdvSceneSource -> "image".equals(gdvSceneSource.getSourceType()) && "tif".equals(gdvSceneSource.getFileType()))
                    .map(GDVSceneSource::getSourceId).collect(Collectors.toList());
            tifSourceIds
                    .stream()
                    .forEach(id -> dscRasterSService.deleteRasterSCopy(sceneId, id));
            log.info("删除场景引用的tif栅格服务：" + tifSourceIds);
            dscGDVSceneConfigDAO.delete(dscGDVSceneConfig);
        } else if (sceneType.equals("DAS")) {
            DscDASceneConfig dscDASceneConfig = dscDASceneConfigDAO.findBySceneId(sceneId);
            dscDASceneConfigDAO.delete(dscDASceneConfig);
            //  如果是分析场景，还需删除场景关联的文件记录及文件内的数据
            dscCatalogService.delete(dscDASceneConfig.getSceneDataRootCatalogId());
        }
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
            case "DAS":
                DscDASceneConfig daSceneConfig = dscDASceneService.getDASceneConfig(sceneId);
                sceneConfig = BeanUtil.toBean(daSceneConfig, JSONObject.class);
                break;
            default:
                sceneConfig = new JSONObject();
                break;
        }
        return CommonResult.success(sceneConfig, "获取场景配置成功！");
    }
}
