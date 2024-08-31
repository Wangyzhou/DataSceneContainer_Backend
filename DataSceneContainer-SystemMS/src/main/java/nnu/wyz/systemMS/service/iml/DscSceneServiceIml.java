package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.db.Page;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.MongoTransactional;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.GetRasterSCopyDTO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.PublishScene2PublicDTO;
import nnu.wyz.systemMS.model.dto.SceneShareImportDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
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
    private DscPublicSceneDAO dscPublicSceneDAO;

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

    @Value("${fileSavePath}")
    private String rootPath;

    @Override
    public CommonResult<PageInfo<DscScene>> getSceneList(PageableDTO pageableDTO, boolean isPublic) {
        String userId = pageableDTO.getCriteria();
        String keyword = pageableDTO.getKeyword(); // 新增关键词参数
        Integer pageIndex = pageableDTO.getPageIndex();
        Integer pageSize = pageableDTO.getPageSize();
        List<String> sceneIds;
        if (isPublic) {
            sceneIds = dscPublicSceneDAO.findAll().stream().map(DscPublicScene::getId).collect(Collectors.toList());
        } else {
            sceneIds = dscUserSceneDAO.findAllByUserId(userId).stream().map(DscUserScene::getSceneId).collect(Collectors.toList());
        }
        List<DscScene> sceneListNoLimit = sceneIds.stream()
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
    public CommonResult<String> deleteScene(String userId, String sceneId, boolean isPublic) {
        // 区分个人和公共
        if (!isPublic) {
            DscUserScene byUserIdAndSceneId = dscUserSceneDAO.findByUserIdAndSceneId(userId, sceneId);
            if (Objects.isNull(byUserIdAndSceneId)) {
                return CommonResult.failed("场景不存在！");
            } else {
                dscUserSceneDAO.delete(byUserIdAndSceneId);
            }
        } else {
            Optional<DscPublicScene> byId = dscPublicSceneDAO.findById(sceneId);
            if (!byId.isPresent()) {
                return CommonResult.failed("场景不存在");
            } else {
                DscPublicScene dscPublicScene = byId.get();
                dscPublicSceneDAO.delete(dscPublicScene);
            }
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

    @Override
    public CommonResult<String> publish2Public(PublishScene2PublicDTO publishScene2PublicDTO) {
        String sceneId = publishScene2PublicDTO.getSceneId();
        String userId = publishScene2PublicDTO.getUserId();
        DscUserScene byUserIdAndSceneId = dscUserSceneDAO.findByUserIdAndSceneId(userId, sceneId);
        if (Objects.isNull(byUserIdAndSceneId)) {
            return CommonResult.failed("场景不存在！");
        }
        Optional<DscScene> byId = dscSceneDAO.findById(sceneId);
        DscScene dscScene = byId.get();
        String type = dscScene.getType();
        if (!type.equals("GDV")) {
            return CommonResult.failed("暂不支持发布该类场景");
        }
        // 添加公共场景记录
        DscPublicScene dscPublicScene = new DscPublicScene();
        String publicSceneId = IdUtil.randomUUID();
        dscPublicScene.setId(publicSceneId)
                .setName(dscScene.getName())
                .setType(type)
                .setCreatedUser(userId);
        dscPublicSceneDAO.insert(dscPublicScene);
        // 复制新的场景信息(只修改必要信息，其他信息沿用原场景信息）
        // 复制缩略图
        String objectKey = userId + File.separator + sceneId + ".png";
        Path thumbnailPath = Paths.get(rootPath + minioConfig.getSceneThumbnailsBucket() + File.separator + objectKey);
        String newObjectKey = userId + File.separator + publicSceneId + ".png";
        Path newThumbnailPath = Paths.get(rootPath + minioConfig.getSceneThumbnailsBucket() + File.separator + newObjectKey);
        try {
            Files.copy(thumbnailPath, newThumbnailPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("复制缩略图失败", e);
        }
        String currentTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        dscScene.setId(publicSceneId)
                .setCreatedTime(currentTime)
                .setUpdatedTime(currentTime)
                .setThumbnail(MessageFormat.format("{0}/{1}/{2}", minioConfig.getEndpoint(), minioConfig.getSceneThumbnailsBucket(), newObjectKey))
                .setIsLocked(true)
                .setEditCount(0L);
        dscSceneDAO.insert(dscScene);
        switch (type) {
            case "GDV":
                this.copyGDVSceneConfig(userId, sceneId, publicSceneId);
                break;
        }
        return CommonResult.success("发布成功!");
    }

    @Override
    public CommonResult<String> importScene(SceneShareImportDTO sceneShareImportDTO) {
        String sceneId = sceneShareImportDTO.getSceneId();
        String userId = sceneShareImportDTO.getUserId();
        Optional<DscScene> byId = dscSceneDAO.findById(sceneId);
        if (!byId.isPresent()) {
            return CommonResult.failed("导入出错：场景信息不存在");
        }
        DscScene dscScene = byId.get();
        String type = dscScene.getType();
        // 暂时只支持GDV
        if (!type.equals("GDV")) {
            return CommonResult.failed("暂不支持导入该类场景");
        }
        // 添加用户场景记录
        DscUserScene dscUserScene = new DscUserScene();
        String newSceneId = IdUtil.randomUUID();
        dscUserScene.setId(IdUtil.randomUUID())
                .setSceneName(dscScene.getName())
                .setSceneId(newSceneId)
                .setUserId(userId);
        dscUserSceneDAO.insert(dscUserScene);
        // 复制新的场景信息(只修改必要信息，其他信息沿用原场景信息）
        // 复制缩略图
        String objectKey = userId + File.separator + sceneId + ".png";
        Path thumbnailPath = Paths.get(rootPath + minioConfig.getSceneThumbnailsBucket() + File.separator + objectKey);
        String newObjectKey = userId + File.separator + newSceneId + ".png";
        Path newThumbnailPath = Paths.get(rootPath + minioConfig.getSceneThumbnailsBucket() + File.separator + newObjectKey);
        try {
            Files.copy(thumbnailPath, newThumbnailPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("复制缩略图失败", e);
        }
        String currentTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        dscScene.setId(newSceneId)
                .setCreatedTime(currentTime)
                .setUpdatedTime(currentTime)
                .setThumbnail(MessageFormat.format("{0}/{1}/{2}", minioConfig.getEndpoint(), minioConfig.getSceneThumbnailsBucket(), newObjectKey))
                .setIsLocked(false)
                .setEditCount(0L);
        dscSceneDAO.insert(dscScene);
        switch (type) {
            case "GDV":
                this.copyGDVSceneConfig(userId, sceneId, newSceneId);
                break;
        }
        return CommonResult.success("导入成功");
    }

    /**
     * @Description: 复制GDV场景配置给目标场景（包括场景源引用）
     * @param userId 目标场景受享用户
     * @param originalSceneId 源场景
     * @param targetSceneId 目标场景
     */
    public void copyGDVSceneConfig(String userId, String originalSceneId, String targetSceneId) {
        // 复制场景配置
        DscGDVSceneConfig sceneConfig = dscGDVSceneConfigDAO.findBySceneId(originalSceneId);
        sceneConfig.setId(IdUtil.objectId())
                .setSceneId(targetSceneId);
        dscGDVSceneConfigDAO.insert(sceneConfig);
        // 增加场景所有源引用
        // 增加除tif栅格外的服务引用，ownerCount+1
        ServiceRefs addRefs = dscGDVSceneService.getSourcesToAddRef(sceneConfig.getSources(), new ArrayList<>());
        log.info("添加场景引用的矢量服务：" + addRefs.getVectorRefs());
        log.info("添加场景引用的非tif栅格服务：" + addRefs.getRasterRefs());
        if (!addRefs.getVectorRefs().isEmpty())
            dscVectorSService.updateOwnerCount(addRefs.getVectorRefs(), true);
        if (!addRefs.getRasterRefs().isEmpty())
            // 更新非tif源的引用
            dscRasterSService.updateOwnerCount(addRefs.getRasterRefs(), true);
        // 增加tif栅格服务引用，ownerCount+1
        List<String> tifSourceIds = sceneConfig.getSources()
                .stream()
                .filter(gdvSceneSource -> "image".equals(gdvSceneSource.getSourceType()) && "tif".equals(gdvSceneSource.getFileType()))
                .map(GDVSceneSource::getSourceId).collect(Collectors.toList());
        tifSourceIds
                .stream()
                .forEach(id -> dscRasterSService.addRasterSCopy(new GetRasterSCopyDTO(userId, targetSceneId, id)));
        log.info("添加场景引用的tif栅格服务：" + tifSourceIds);
    }
}
