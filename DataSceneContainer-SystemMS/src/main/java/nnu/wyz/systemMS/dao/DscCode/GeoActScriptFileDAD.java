package nnu.wyz.systemMS.dao.DscCode;

import nnu.wyz.systemMS.model.entity.codeModel.GeoActFolder;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActScriptFile;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface GeoActScriptFileDAD extends MongoRepository<GeoActScriptFile, String> {
    Optional<GeoActScriptFile> findByParentIdAndFileNameAndType(String id, String fileName, String type);
    List<GeoActScriptFile> findByParentId(String parentId);
    List<GeoActScriptFile> findByExecutorAndSceneId(String executor, String sceneId);
}
