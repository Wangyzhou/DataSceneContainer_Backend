package nnu.wyz.systemMS.service.DscCode;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActFileNode;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActFolderDTO;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActScriptDTO;
import nnu.wyz.systemMS.model.dto.StringPackage;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActFolder;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActScriptFile;

public interface GeoActService {
    CommonResult<?> initFileTree(String userId, String sceneId);
    CommonResult<?> createSingleFile(GeoActScriptDTO geoActScriptFile);
    CommonResult<?> deleteSingleFile(String fileId);
    CommonResult<?> updateSingleFile(String fileId, StringPackage updateScript);
    CommonResult<?> createFolder(GeoActFolderDTO geoActFolderDTO);
    CommonResult<?> deleteFolder(String id);
    CommonResult<?> getScriptContent(String scriptId);
    CommonResult<?> renameNode(GeoActFileNode geoActFileNode);
}
