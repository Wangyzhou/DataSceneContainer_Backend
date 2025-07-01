package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.MongoTransactional;
import nnu.wyz.systemMS.dao.DscGDVSceneConfigDAO;
import nnu.wyz.systemMS.dao.DscSceneDAO;
import nnu.wyz.systemMS.dao.DscUserSceneDAO;
import nnu.wyz.systemMS.model.dto.MapPublishDTO;
import nnu.wyz.systemMS.model.dto.SaveGDVSceneDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.DscGDVSceneService;
import nnu.wyz.systemMS.service.DscRasterSService;
import nnu.wyz.systemMS.service.DscVectorSService;
import nnu.wyz.systemMS.utils.ImageUtil;
import nnu.wyz.systemMS.utils.MimeTypesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/14 14:34
 */

@Service
@Slf4j
public class DscGDVSceneServiceIml implements DscGDVSceneService {

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private DscSceneDAO dscSceneDAO;

    @Autowired
    private DscUserSceneDAO dscUserSceneDAO;

    @Autowired
    private DscGDVSceneConfigDAO dscGDVSceneConfigDAO;

    @Autowired
    private DscVectorSService dscVectorSService;

    @Autowired
    private DscRasterSService dscRasterSService;

    @Value("${fileTempPath}")
    private String fileTempPath;

    private final static String SCENE_TYPE = "GDV";

    @Override
    public CommonResult<DscScene> saveGDVScene(SaveGDVSceneDTO saveGDVSceneDTO) {
        String userId = saveGDVSceneDTO.getUserId();
        String sceneId = Objects.equals(saveGDVSceneDTO.getSceneId(), "") ? IdUtil.randomUUID() : saveGDVSceneDTO.getSceneId();
        MultipartFile thumbnail = saveGDVSceneDTO.getThumbnail();
        String contentType = thumbnail.getContentType();
        String ext = MimeTypesUtil.getDefaultExt(contentType);
        DscUserScene isExist = dscUserSceneDAO.findByUserIdAndSceneName(userId, saveGDVSceneDTO.getName());
        if (!Objects.isNull(isExist) && !isExist.getSceneId().equals(sceneId)) {
            return CommonResult.failed("与现有场景名重复，请修改名称后重新创建！");
        }
        // 图片暂存磁盘
        File thumbnailDir = new File(fileTempPath);
        if (!thumbnailDir.exists()) {
            thumbnailDir.mkdirs();
        }
        File thumbnailDisk = new File(fileTempPath + File.separator + IdUtil.fastSimpleUUID() + "." + ext);
        try {
            thumbnail.transferTo(thumbnailDisk);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MultipartFile thumbnailAfterCompress = null;
        try {
            //压缩图片
            log.info("压缩前：" + thumbnail.getSize() + "字节");
            thumbnailAfterCompress = ImageUtil.compressImageFile(thumbnailDisk);
            log.info("压缩后:" + thumbnailAfterCompress.getSize() + "字节");
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("img/png");
            objectMetadata.setContentLength(thumbnailAfterCompress.getSize());
            String objectKey = MessageFormat.format("{0}/{1}.{2}", userId, sceneId, ext);
            boolean isThumbnailExist = amazonS3.doesObjectExist(minioConfig.getSceneThumbnailsBucket(), objectKey);
            if (isThumbnailExist) {
                amazonS3.deleteObject(minioConfig.getSceneThumbnailsBucket(), objectKey);
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(minioConfig.getSceneThumbnailsBucket(), objectKey, thumbnailAfterCompress.getInputStream(), objectMetadata);
            amazonS3.putObject(putObjectRequest);
            String createdTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            Optional<DscScene> byId = dscSceneDAO.findById(sceneId);
            DscScene dscScene;
            if (!byId.isPresent()) {
                dscScene = new DscScene(sceneId, saveGDVSceneDTO.getName(), SCENE_TYPE, MessageFormat.format("{0}/{1}/{2}", minioConfig.getEndpoint(), minioConfig.getSceneThumbnailsBucket(), objectKey), userId, 1L, createdTime, createdTime, false, 16, null);
            } else {
                dscScene = byId.get();
                dscScene.setName(saveGDVSceneDTO.getName());
                dscScene.setEditCount(dscScene.getEditCount() + 1);
                dscScene.setUpdatedTime(createdTime);
            }
            dscSceneDAO.save(dscScene);
            DscUserScene userIdAndSceneId = dscUserSceneDAO.findByUserIdAndSceneId(userId, sceneId);
            String userSceneId = Objects.isNull(userIdAndSceneId) ? IdUtil.randomUUID() : userIdAndSceneId.getId();
            DscUserScene dscUserScene = new DscUserScene();
            dscUserScene.setId(userSceneId)
                    .setSceneName(saveGDVSceneDTO.getName())
                    .setUserId(userId)
                    .setSceneId(sceneId);
            dscUserSceneDAO.save(dscUserScene);
            DscGDVSceneConfig gdvSceneConfig = dscGDVSceneConfigDAO.findBySceneId(sceneId);

            // 更新场景源的引用（根据当前场景源列表对比上一次，将新增的源添加引用，删除的源删除引用）
            List<GDVSceneSource> lastSources = Objects.isNull(gdvSceneConfig) ? new ArrayList<>() : gdvSceneConfig.getSources();
            ServiceRefs addRefs = getSourcesToAddRef(lastSources, saveGDVSceneDTO.getSources());
            log.info("新增引用的矢量服务：" + addRefs.getVectorRefs());
            log.info("新增引用的栅格服务：" + addRefs.getRasterRefs());
            if (!addRefs.getVectorRefs().isEmpty()) dscVectorSService.updateOwnerCount(addRefs.getVectorRefs(), true);
            if (!addRefs.getRasterRefs().isEmpty()) dscRasterSService.updateOwnerCount(addRefs.getRasterRefs(), true);
            ServiceRefs minusRefs = getSourcesToMinusRef(lastSources, saveGDVSceneDTO.getSources());
            log.info("删除引用的矢量服务：" + minusRefs.getVectorRefs());
            log.info("删除引用的栅格服务：" + minusRefs.getRasterRefs());
            if (!minusRefs.getVectorRefs().isEmpty())
                dscVectorSService.updateOwnerCount(minusRefs.getVectorRefs(), false);
            if (!minusRefs.getRasterRefs().isEmpty())
                dscRasterSService.updateOwnerCount(minusRefs.getRasterRefs(), false);


            String configId = Objects.isNull(gdvSceneConfig) ? IdUtil.objectId() : gdvSceneConfig.getId();
            DscGDVSceneConfig dscGDVSceneConfig = new DscGDVSceneConfig();
            dscGDVSceneConfig.setId(configId)
                    .setSceneId(sceneId)
                    .setSources(saveGDVSceneDTO.getSources())
                    .setLayers(saveGDVSceneDTO.getLayers())
                    .setPos(saveGDVSceneDTO.getPos())
                    .setMapParams(saveGDVSceneDTO.getMapParams())
                    .setSprite(saveGDVSceneDTO.getSprite());
            dscGDVSceneConfigDAO.save(dscGDVSceneConfig);
            return CommonResult.success(dscScene, "场景保存成功！");
        } catch (IOException e) {
            e.printStackTrace();
            return CommonResult.success("场景保存失败！");
        } finally {
            thumbnailDisk.delete();
        }
    }

    @Override
    public DscGDVSceneConfig getGDVSceneConfig(String sceneId) {
        return dscGDVSceneConfigDAO.findBySceneId(sceneId);
    }

    @Override
    public CommonResult<String> publishMap(MapPublishDTO mapPublishDTO) {
        String sceneId = mapPublishDTO.getSceneId();
        JsonNode mapStyle = mapPublishDTO.getMapStyle();
        Optional<DscScene> byId = dscSceneDAO.findById(sceneId);
        if (!byId.isPresent()) {
            return CommonResult.failed("场景不存在");
        }
        DscScene dscScene = byId.get();
        try {
            // 把 JsonNode 转换为字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mapStyle);
            byte[] contentBytes = jsonContent.getBytes(StandardCharsets.UTF_8);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(contentBytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentBytes.length);
            metadata.setContentType("application/json");
            // 上传到 MinIO
            String filename = IdUtil.randomUUID() + ".json";
            String objectKey = MessageFormat.format("{0}/{1}", sceneId, filename);
            boolean isFileExist = amazonS3.doesObjectExist(minioConfig.getMapStyleBucket(), objectKey);
            if (isFileExist) {
                amazonS3.deleteObject(minioConfig.getMapStyleBucket(), objectKey);
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(minioConfig.getMapStyleBucket(), objectKey, inputStream, metadata);
            amazonS3.putObject(putObjectRequest);
            // 更新到场景属性中
            String publishUrl = MessageFormat.format("{0}/{1}/{2}", minioConfig.getEndpoint(), minioConfig.getMapStyleBucket(), objectKey);
            dscScene.setPublishUrl(publishUrl);
            return CommonResult.success(publishUrl, "发布成功");
        } catch (Exception e) {
            log.error("发布失败" + e.getMessage());
            e.printStackTrace();
            return CommonResult.failed("发布失败");
        }
    }

    /**
     * 比较两次场景配置中源列表的变化，获取新增的源
     *
     * @param lastSources
     * @param currentSources
     * @return
     */
    @Override
    public ServiceRefs getSourcesToAddRef(List<GDVSceneSource> lastSources, List<GDVSceneSource> currentSources) {
        List<String> addVecRefList = new ArrayList<>();
        List<String> addRasRefList = new ArrayList<>();
        for (GDVSceneSource currentSource : currentSources) {
            // 跳过tif栅格服务，因为这类服务会在添加源和删除源时单独更新
            if ("image".equals(currentSource.getSourceType()) && "tif".equals(currentSource.getFileType())) {
                continue;
            }
            // 跳过聚合源，因为这类源实际上是去掉_cluster之后的id的源的同一引用
            if (currentSource.getSourceId().endsWith("_cluster")) continue;
            boolean existInLast = false;
            for (GDVSceneSource lastSource : lastSources) {
                if (currentSource.getSourceId().equals(lastSource.getSourceId())) {
                    existInLast = true;
                    break;
                }
            }
            if (!existInLast) {
                if ("vector".equals(currentSource.getSourceType()) || "geojson".equals(currentSource.getSourceType())) {
                    addVecRefList.add(currentSource.getSourceId());
                } else {
                    addRasRefList.add(currentSource.getSourceId());
                }
            }
        }
        ServiceRefs serviceRefs = new ServiceRefs(addVecRefList, addRasRefList);
        return serviceRefs;
    }

    /**
     * 比较两次场景配置中源列表的变化，获取去除的源
     *
     * @param lastSources
     * @param currentSources
     * @return
     */

    @Override
    public ServiceRefs getSourcesToMinusRef(List<GDVSceneSource> lastSources, List<GDVSceneSource> currentSources) {
        List<String> minusVecRefList = new ArrayList<>();
        List<String> minusRasRefList = new ArrayList<>();
        for (GDVSceneSource lastSource : lastSources) {
            // 跳过tif栅格服务，因为这类服务会在添加源和删除源时单独更新
            if ("image".equals(lastSource.getSourceType()) && "tif".equals(lastSource.getFileType())) {
                continue;
            }
            // 跳过聚合源，因为这类源实际上是去掉_cluster之后的id的源的同一引用
            if (lastSource.getSourceId().endsWith("_cluster")) continue;
            boolean existInCurrent = false;
            for (GDVSceneSource currentSource : currentSources) {
                if (currentSource.getSourceId().equals(lastSource.getSourceId())) {
                    existInCurrent = true;
                    break;
                }
            }
            if (!existInCurrent) {
                if ("vector".equals(lastSource.getSourceType()) || "geojson".equals(lastSource.getSourceType())) {
                    minusVecRefList.add(lastSource.getSourceId());
                } else {
                    minusRasRefList.add(lastSource.getSourceId());
                }

            }
        }
        ServiceRefs serviceRefs = new ServiceRefs(minusVecRefList, minusRasRefList);
        return serviceRefs;
    }

}
