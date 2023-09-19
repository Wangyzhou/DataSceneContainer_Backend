package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.amazonaws.partitions.PartitionRegionImpl;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.MongoTransactional;
import nnu.wyz.systemMS.dao.DscGDVSceneConfigDAO;
import nnu.wyz.systemMS.dao.DscSceneDAO;
import nnu.wyz.systemMS.dao.DscUserSceneDAO;
import nnu.wyz.systemMS.model.dto.CreateGDVSceneDTO;
import nnu.wyz.systemMS.model.entity.DscGDVSceneConfig;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.DscUserScene;
import nnu.wyz.systemMS.service.DscGDVSceneService;
import nnu.wyz.systemMS.utils.MimeTypesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
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
    public CommonResult<String> createGDVScene(CreateGDVSceneDTO createGDVSceneDTO) {
        String userId = createGDVSceneDTO.getUserId();
        String sceneId = IdUtil.randomUUID();
        MultipartFile thumbnail = createGDVSceneDTO.getThumbnail();
        String contentType = thumbnail.getContentType();
        String ext = MimeTypesUtil.getDefaultExt(contentType);
        DscUserScene isExist = dscUserSceneDAO.findByUserIdAndSceneName(userId, createGDVSceneDTO.getName());
        if(!Objects.isNull(isExist)) {
            return CommonResult.failed("场景名重复，请修改名称后重新创建！");
        }
        try {
            InputStream thumbnailInputStream = thumbnail.getInputStream();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("img/png");
            String objectKey = MessageFormat.format("{0}/{1}.{2}", userId, sceneId, ext);
            PutObjectRequest putObjectRequest = new PutObjectRequest(minioConfig.getSceneThumbnailsBucket(), objectKey, thumbnailInputStream, objectMetadata);
            amazonS3.putObject(putObjectRequest);
            String createdTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            DscScene dscScene = new DscScene(sceneId, createGDVSceneDTO.getName(),SCENE_TYPE, MessageFormat.format("{0}/{1}/{2}", minioConfig.getEndpoint(), minioConfig.getSceneThumbnailsBucket(), objectKey),userId, 0L, createdTime,createdTime,false, 16);
            dscSceneDAO.save(dscScene);
            DscUserScene dscUserScene = new DscUserScene();
            dscUserScene.setId(IdUtil.randomUUID())
                    .setSceneName(createGDVSceneDTO.getName())
                    .setUserId(userId)
                    .setSceneId(sceneId);
            dscUserSceneDAO.insert(dscUserScene);
            DscGDVSceneConfig dscGDVSceneConfig = new DscGDVSceneConfig();
            dscGDVSceneConfig.setId(IdUtil.objectId())
                    .setSceneId(sceneId)
                    .setSources(createGDVSceneDTO.getSources())
                    .setLayers(createGDVSceneDTO.getLayers())
                    .setPos(createGDVSceneDTO.getPos())
                    .setMapParams(createGDVSceneDTO.getMapParams());
            dscGDVSceneConfigDAO.save(dscGDVSceneConfig);
            return CommonResult.success("场景创建成功！");
        } catch (IOException e) {
            log.error("创建场景失败！");
            e.printStackTrace();
            throw new RuntimeException("创建场景失败！");
        }
    }
}
