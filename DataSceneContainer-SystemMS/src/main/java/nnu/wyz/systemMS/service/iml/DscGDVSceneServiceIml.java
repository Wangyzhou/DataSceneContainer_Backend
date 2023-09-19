package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.MongoTransactional;
import nnu.wyz.systemMS.dao.DscGDVSceneConfigDAO;
import nnu.wyz.systemMS.dao.DscSceneDAO;
import nnu.wyz.systemMS.dao.DscUserSceneDAO;
import nnu.wyz.systemMS.model.dto.SaveGDVSceneDTO;
import nnu.wyz.systemMS.model.entity.DscGDVSceneConfig;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.DscUserScene;
import nnu.wyz.systemMS.service.DscGDVSceneService;
import nnu.wyz.systemMS.utils.MimeTypesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Objects;

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
    private final static String SCENE_TYPE = "GDV";
    @Override
    @MongoTransactional
    public CommonResult<String> saveGDVScene(SaveGDVSceneDTO saveGDVSceneDTO) {
        String userId = saveGDVSceneDTO.getUserId();
        String sceneId = Objects.equals(saveGDVSceneDTO.getSceneId(), "") ? IdUtil.randomUUID() : saveGDVSceneDTO.getSceneId();
        MultipartFile thumbnail = saveGDVSceneDTO.getThumbnail();
        String contentType = thumbnail.getContentType();
        String ext = MimeTypesUtil.getDefaultExt(contentType);
        DscUserScene isExist = dscUserSceneDAO.findByUserIdAndSceneName(userId, saveGDVSceneDTO.getName());
        if(!Objects.isNull(isExist) && !isExist.getSceneId().equals(sceneId)) {
            return CommonResult.failed("与现有场景名重复，请修改名称后重新创建！");
        }
        try {
            InputStream thumbnailInputStream = thumbnail.getInputStream();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("img/png");
            String objectKey = MessageFormat.format("{0}/{1}.{2}", userId, sceneId, ext);
            boolean isThumbnailExist = amazonS3.doesObjectExist(minioConfig.getSceneThumbnailsBucket(), objectKey);
            if(isThumbnailExist) {
                amazonS3.deleteObject(minioConfig.getSceneThumbnailsBucket(), objectKey);
            }
            PutObjectRequest putObjectRequest = new PutObjectRequest(minioConfig.getSceneThumbnailsBucket(), objectKey, thumbnailInputStream, objectMetadata);
            amazonS3.putObject(putObjectRequest);
            String createdTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            DscScene dscScene = new DscScene(sceneId, saveGDVSceneDTO.getName(),SCENE_TYPE, MessageFormat.format("{0}/{1}/{2}", minioConfig.getEndpoint(), minioConfig.getSceneThumbnailsBucket(), objectKey),userId, 0L, createdTime,createdTime,false, 16);
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
            String configId = Objects.isNull(gdvSceneConfig) ? IdUtil.objectId() : gdvSceneConfig.getId();
            DscGDVSceneConfig dscGDVSceneConfig = new DscGDVSceneConfig();
            dscGDVSceneConfig.setId(configId)
                    .setSceneId(sceneId)
                    .setSources(saveGDVSceneDTO.getSources())
                    .setLayers(saveGDVSceneDTO.getLayers())
                    .setPos(saveGDVSceneDTO.getPos())
                    .setMapParams(saveGDVSceneDTO.getMapParams());
            dscGDVSceneConfigDAO.save(dscGDVSceneConfig);
            return CommonResult.success("场景保存成功！");
        } catch (IOException e) {
            log.error("场景保存失败！");
            e.printStackTrace();
            throw new RuntimeException("场景保存失败！");
        }
    }
    @Override
    public DscGDVSceneConfig getGDVSceneConfig(String sceneId) {
        return dscGDVSceneConfigDAO.findBySceneId(sceneId);
    }
}
