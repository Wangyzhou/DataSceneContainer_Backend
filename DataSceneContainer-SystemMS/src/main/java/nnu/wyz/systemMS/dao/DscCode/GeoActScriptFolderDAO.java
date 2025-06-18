package nnu.wyz.systemMS.dao.DscCode;

import nnu.wyz.systemMS.model.entity.codeModel.GeoActFolder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GeoActScriptFolderDAO extends MongoRepository<GeoActFolder, String> {
    public boolean existsByFolderNameAndSceneIdAndParentId(String folderName, String sceneId, String parentId);
    List<GeoActFolder> findByParentId(String parentId);
    List<GeoActFolder> findByExecutorAndSceneId(String executor, String sceneId);
}
