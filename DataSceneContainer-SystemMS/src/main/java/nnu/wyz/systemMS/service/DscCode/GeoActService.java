package nnu.wyz.systemMS.service.DscCode;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscCode.*;
import nnu.wyz.systemMS.model.dto.FundamentalPackage.StringPackage;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActFolder;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActScriptFile;

import java.io.IOException;

public interface GeoActService {
    CommonResult<?> initFileTree(String userId, String sceneId);
    CommonResult<?> createSingleFile(GeoActScriptDTO geoActScriptFile);
    CommonResult<?> deleteSingleFile(String fileId);
    CommonResult<?> updateSingleFile(String fileId, String script);
    CommonResult<?> createFolder(GeoActFolderDTO geoActFolderDTO);
    CommonResult<?> deleteFolder(String id);
    CommonResult<?> getScriptContent(String scriptId);
    CommonResult<?> renameNode(GeoActFileNode geoActFileNode);
    CommonResult<?> submitGeoActTask(GeoActFileNode geoActFileNode);
    CommonResult<?> encapsulation(GeoActModelDTO geoActModelDTO);
    CommonResult<?> getModelScript(String id);
    CommonResult<?> deleteModelExScript(String id);
    CommonResult<?> deleteModel(String id);
    CommonResult<?> preAstroid(String script);
    CommonResult<?> getShxAndDbf(String id, String catalogId);
    CommonResult<?> parseCode(GeoActParseParamsDTO geoActParseParamsDTO);
}
