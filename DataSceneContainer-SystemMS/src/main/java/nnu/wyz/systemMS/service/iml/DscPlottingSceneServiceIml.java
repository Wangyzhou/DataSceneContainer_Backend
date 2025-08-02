package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.util.IdUtil;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscPlottingSceneDAO;
import nnu.wyz.systemMS.model.dto.CreatePlottingSceneDTO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.DscPlottingScene;
import nnu.wyz.systemMS.model.entity.PageInfo;
import nnu.wyz.systemMS.service.DscPlottingSceneService;
import nnu.wyz.systemMS.utils.ImageUtil;
import nnu.wyz.systemMS.utils.MimeTypesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tjk
 * @date 2025/8/1
 * @Description
 */
@Service
public class DscPlottingSceneServiceIml implements DscPlottingSceneService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private DscPlottingSceneDAO dscPlottingSceneDAO;

    @Value("${fileTempPath}")
    private String fileTempPath;

    @Override
    public CommonResult<DscPlottingScene> createScene(CreatePlottingSceneDTO dto) {
        String sceneId = UUID.randomUUID().toString();
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        MultipartFile thumbnailFile = dto.getThumbnail();
        String thumbnailUrl = null;

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            try {
                String contentType = thumbnailFile.getContentType();
                String ext = MimeTypesUtil.getDefaultExt(contentType);
                File tempFile = new File(fileTempPath + File.separator + IdUtil.fastSimpleUUID() + "." + ext);
                thumbnailFile.transferTo(tempFile);

                MultipartFile compressedFile = ImageUtil.compressImageFile(tempFile);
                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentType(contentType);
                objectMetadata.setContentLength(compressedFile.getSize());

                String objectKey = MessageFormat.format("{0}/{1}.{2}", dto.getCreatedUser(), sceneId, ext);
                amazonS3.putObject(new PutObjectRequest(minioConfig.getSceneThumbnailsBucket(), objectKey, compressedFile.getInputStream(), objectMetadata));
                thumbnailUrl = MessageFormat.format("{0}/{1}/{2}", minioConfig.getEndpoint(), minioConfig.getSceneThumbnailsBucket(), objectKey);

                tempFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
                return CommonResult.failed("缩略图上传失败: " + e.getMessage());
            }
        }

        DscPlottingScene entity = new DscPlottingScene()
                .setId(sceneId)
                .setName(dto.getName())
                .setSmlString(dto.getSmlString())
                .setThumbnail(thumbnailUrl)
                .setCreatedUser(dto.getCreatedUser())
                .setCreatedTime(now)
                .setUpdatedTime(now);

        try {
            dscPlottingSceneDAO.save(entity);
            return CommonResult.success(entity);
        } catch (Exception e) {
            return CommonResult.failed("场景保存失败：" + e.getMessage());
        }
    }

    @Override
    public CommonResult<DscPlottingScene> updateScene(String sceneId, CreatePlottingSceneDTO dto) {
        Optional<DscPlottingScene> sceneOptional = dscPlottingSceneDAO.findById(sceneId);
        if (!sceneOptional.isPresent()) {
            return CommonResult.failed("更新失败，场景不存在");
        }

        DscPlottingScene scene = sceneOptional.get();

        // 处理缩略图替换（如果上传了新图）
        MultipartFile newThumbnail = dto.getThumbnail();
        String newThumbnailUrl = scene.getThumbnail();
        if (newThumbnail != null && !newThumbnail.isEmpty()) {
            try {
                // 删除旧图
                String oldThumbnailUrl = scene.getThumbnail();
                if (oldThumbnailUrl != null && !oldThumbnailUrl.isEmpty()) {
                    int index = oldThumbnailUrl.indexOf(scene.getCreatedUser());
                    if (index != -1) {
                        String objectKey = oldThumbnailUrl.substring(index);
                        amazonS3.deleteObject(new DeleteObjectRequest(minioConfig.getSceneThumbnailsBucket(), objectKey));
                    }
                }

                // 上传新图
                String contentType = newThumbnail.getContentType();
                String ext = MimeTypesUtil.getDefaultExt(contentType);
                File tempFile = new File(fileTempPath + File.separator + IdUtil.fastSimpleUUID() + "." + ext);
                newThumbnail.transferTo(tempFile);

                MultipartFile compressedFile = ImageUtil.compressImageFile(tempFile);
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(contentType);
                metadata.setContentLength(compressedFile.getSize());

                String objectKey = MessageFormat.format("{0}/{1}.{2}", scene.getCreatedUser(), sceneId, ext);
                amazonS3.putObject(new PutObjectRequest(minioConfig.getSceneThumbnailsBucket(), objectKey, compressedFile.getInputStream(), metadata));
                newThumbnailUrl = MessageFormat.format("{0}/{1}/{2}", minioConfig.getEndpoint(), minioConfig.getSceneThumbnailsBucket(), objectKey);

                tempFile.delete();
            } catch (IOException e) {
                e.printStackTrace();
                return CommonResult.failed("缩略图更新失败：" + e.getMessage());
            }
        }

        // 更新属性
        scene.setName(dto.getName());
        scene.setSmlString(dto.getSmlString());
        scene.setThumbnail(newThumbnailUrl);
        scene.setUpdatedTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            dscPlottingSceneDAO.save(scene);
            return CommonResult.success(scene, "更新成功");
        } catch (Exception e) {
            return CommonResult.failed("更新失败：" + e.getMessage());
        }
    }


    @Override
    public CommonResult<String> deleteScene(String userId, String sceneId) {
        Optional<DscPlottingScene> sceneOptional = dscPlottingSceneDAO.findById(sceneId);
        if (!sceneOptional.isPresent()) {
            return CommonResult.failed("删除失败，场景不存在");
        }

        DscPlottingScene scene = sceneOptional.get();
        if (!userId.equals(scene.getCreatedUser())) {
            return CommonResult.failed("删除失败，您无权删除该场景");
        }

        // 删除场景缩略图在 S3 的文件
        String thumbnailUrl = scene.getThumbnail();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            int index = thumbnailUrl.indexOf(userId);
            if (index != -1) {
                String objectKey = thumbnailUrl.substring(index);
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(
                        minioConfig.getSceneThumbnailsBucket(),
                        objectKey
                );
                amazonS3.deleteObject(deleteObjectRequest);
            }
        }

        // 删除场景数据库记录
        dscPlottingSceneDAO.deleteById(sceneId);
        return CommonResult.success("删除成功！");
    }



    @Override
    public CommonResult<PageInfo<DscPlottingScene>> getSceneList(PageableDTO pageableDTO) {
        String keyword = pageableDTO.getKeyword();
        Integer pageIndex = pageableDTO.getPageIndex();
        Integer pageSize = pageableDTO.getPageSize();

        // 获取全部场景
        List<DscPlottingScene> allScenes = dscPlottingSceneDAO.findAll();

        // 关键词过滤（如果 keyword 不为空则执行模糊过滤）
        List<DscPlottingScene> filteredScenes = allScenes.stream()
                .filter(scene -> keyword == null || keyword.isEmpty() || scene.getName().contains(keyword))
                .sorted(Comparator.comparing(DscPlottingScene::getUpdatedTime).reversed())
                .collect(Collectors.toList());

        // 分页处理
        int fromIndex = Math.max(0, (pageIndex - 1) * pageSize);
        int toIndex = Math.min(filteredScenes.size(), fromIndex + pageSize);
        List<DscPlottingScene> paginatedList = fromIndex >= filteredScenes.size() ? new ArrayList<>() : filteredScenes.subList(fromIndex, toIndex);

        // 组装分页信息
        PageInfo<DscPlottingScene> pageInfo = new PageInfo<>(
                paginatedList,
                filteredScenes.size(),
                (int) Math.ceil((double) filteredScenes.size() / pageSize)
        );

        return CommonResult.success(pageInfo, "获取标绘场景列表成功！");
    }

}
