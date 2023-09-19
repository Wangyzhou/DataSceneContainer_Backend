package nnu.wyz.systemMS.service.iml;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.MongoTransactional;
import nnu.wyz.systemMS.dao.DscGDVSceneConfigDAO;
import nnu.wyz.systemMS.dao.DscSceneDAO;
import nnu.wyz.systemMS.dao.DscUserSceneDAO;
import nnu.wyz.systemMS.model.entity.DscGDVSceneConfig;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.DscUserScene;
import nnu.wyz.systemMS.service.DscSceneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private MinioConfig minioConfig;

    @Autowired
    private AmazonS3 amazonS3;
    @Override
    public CommonResult<List<DscScene>> getSceneList(String userId) {
        List<DscUserScene> allByUserId = dscUserSceneDAO.findAllByUserId(userId);
        Iterator<DscUserScene> iterator = allByUserId.iterator();
        ArrayList<DscScene> sceneList = new ArrayList<>();
        while (iterator.hasNext()) {
            DscUserScene dscUserScene = iterator.next();
            String sceneId = dscUserScene.getSceneId();
            Optional<DscScene> byId = dscSceneDAO.findById(sceneId);
            if(byId.isPresent()){
                DscScene dscScene = byId.get();
                sceneList.add(dscScene);
            }
        }
        return CommonResult.success(sceneList, "获取场景列表成功！");
    }

    @Override
    @MongoTransactional
    public CommonResult<String> deleteScene(String userId, String sceneId) {
        DscUserScene byUserIdAndSceneId = dscUserSceneDAO.findByUserIdAndSceneId(userId, sceneId);
        if(Objects.isNull(byUserIdAndSceneId)) {
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
        dscSceneDAO.delete(dscScene);
        dscGDVSceneConfigDAO.delete(dscGDVSceneConfig);
        return CommonResult.success("删除场景成功！");
    }
}
