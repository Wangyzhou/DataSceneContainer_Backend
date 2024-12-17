package nnu.wyz.systemMS.controller;
import nnu.wyz.systemMS.model.dto.DscCodeFileDTO;
import nnu.wyz.systemMS.model.entity.DscCodeFile;
import nnu.wyz.systemMS.service.DscCodeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dsc-code-file")
public class DscCodeFileController {

    @Autowired
    private DscCodeFileService dscCodeFileService;

    @PostMapping("/save")
    public ResponseEntity<String> saveCodeFile(@RequestBody DscCodeFileDTO codeFileDTO) {
        // 调用 Service 层保存文件
        dscCodeFileService.saveCodeFile(codeFileDTO);
        return ResponseEntity.ok("Code file saved successfully.");
    }

    @GetMapping("/getFileList")
    public List<DscCodeFile> getFileList(@RequestBody String userId) {
        return dscCodeFileService.getFileList(userId);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCodeFile(@PathVariable String id) {
        // 调用 Service 层删除文件
        boolean isDeleted = dscCodeFileService.deleteCodeFile(id);

        if (isDeleted) {
            return ResponseEntity.ok("Code file deleted successfully.");
        } else {
            return ResponseEntity.status(404).body("Code file not found.");
        }
    }

}

