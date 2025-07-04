package nnu.wyz.systemMS.controller.DscCode;

import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActFileNode;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActFolderDTO;
import nnu.wyz.systemMS.model.dto.DscCode.GeoActScriptDTO;
import nnu.wyz.systemMS.model.dto.FundamentalPackage.StringPackage;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActFolder;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActScriptFile;
import nnu.wyz.systemMS.service.DscCode.GeoActService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/dsc-geo-act")
public class GeoActController {

    @Autowired
    private GeoActService geoActService;

    @GetMapping("/get-project-list/{userId}/{sceneId}")
    public CommonResult<?> getProjectList(@PathVariable String userId, @PathVariable String sceneId) {
        return geoActService.initFileTree(userId, sceneId);
    }

//    @PostMapping("/create-project/{userId}/{sceneId}")
//    public CommonResult<?> createProject(@PathVariable String userId, @PathVariable String sceneId) {}

    @PostMapping("/create-file")
    public CommonResult<?> createSingleFile(@RequestBody GeoActScriptDTO geoActScriptDTO) {
        return geoActService.createSingleFile(geoActScriptDTO);
    }

    @PostMapping("/delete-file/{id}")
    public CommonResult<?> deleteFile(@PathVariable String id) {
        return geoActService.deleteSingleFile(id);
    }

    @PostMapping("/update-file/{fileId}")
    public CommonResult<?> updateFile(@PathVariable String fileId, @RequestBody StringPackage updatedScript) {
        return geoActService.updateSingleFile(fileId, updatedScript);
    }

    @PostMapping("/rename-node")
    public CommonResult<?> renameFile(@RequestBody GeoActFileNode geoActFileNode) {
        return geoActService.renameNode(geoActFileNode);
    }

    @PostMapping("/create-folder")
    public CommonResult<?> createFolder(@RequestBody GeoActFolderDTO geoActFolderDTO){
        log.info("{}/{}/{}/{}", geoActFolderDTO.getFolderName(), geoActFolderDTO.getExecutor(), geoActFolderDTO.getSceneId(), geoActFolderDTO.getParentId());
        return geoActService.createFolder(geoActFolderDTO);
    }

    @PostMapping("/delete-folder/{id}")
    public CommonResult<?> deleteFolder(@PathVariable String id){
        return geoActService.deleteFolder(id);
    }


    @GetMapping("/get-script-content/{id}")
    public CommonResult<?> getScriptContent(@PathVariable String id){
        return geoActService.getScriptContent(id);
    }

    @PostMapping("/submit-script-task")
    public CommonResult<?> submitScriptTask(@RequestBody GeoActFileNode geoActFileNode){
        return geoActService.submitGeoActTask(geoActFileNode);
    }
}
