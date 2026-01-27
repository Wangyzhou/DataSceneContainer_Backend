package nnu.wyz.systemMS.controller.DscCode;

import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscCode.*;
import nnu.wyz.systemMS.model.dto.FundamentalPackage.StringPackage;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActFolder;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActScriptFile;
import nnu.wyz.systemMS.service.DscCode.GeoActService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
    public CommonResult<?> updateFile(@PathVariable String fileId, @RequestBody StringPackage script) {
        return geoActService.updateSingleFile(fileId, script.getContent());
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

    @PostMapping("/encapsulation")
    public CommonResult<?> encapsulation(@RequestBody GeoActModelDTO geoActModelDTO){
        return geoActService.encapsulation(geoActModelDTO);
    }

    @PostMapping("/get-model-script/{id}")
    public CommonResult<?> getModelScript(@PathVariable String id){
        return geoActService.getModelScript(id);
    }

    @PostMapping("/delete-model-ex-script/{id}")
    public CommonResult<?> deleteModelExScript(@PathVariable String id){
        return geoActService.deleteModelExScript(id);
    }

    @DeleteMapping("/delete-model/{id}")
    public CommonResult<?> deleteModel(@PathVariable String id){
        return geoActService.deleteModel(id);
    }
    
    @PostMapping("/pre-astroid")
    public CommonResult<?> preAstroid(@RequestBody StringPackage script){
        return geoActService.preAstroid(script.getContent());
    }

    @PostMapping("/get-shx-and-dbf/{id}/{catalogId}")
    public CommonResult<?> getShxAndDbf(@PathVariable String id, @PathVariable String catalogId){
        return geoActService.getShxAndDbf(id, catalogId);
    }

    @PostMapping("/parse-code")
    public CommonResult<?> parseCode(@RequestBody GeoActParseParamsDTO parseParamsDTO){
        return geoActService.parseCode(parseParamsDTO);
    }
}
