package nnu.wyz.systemMS.controller.DscCode;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DscCodeFileDTO;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeFile;
import nnu.wyz.systemMS.service.DscCode.DscCodeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/dsc-code-file")
public class DscCodeFileController {

    @Autowired
    private DscCodeFileService dscCodeFileService;

    @PostMapping("/save")
    public CommonResult<String> saveCodeFile(@RequestBody @Valid DscCodeFileDTO codeFileDTO) {
        // 调用 Service 层保存文件
        dscCodeFileService.saveCodeFile(codeFileDTO);
        return CommonResult.success("Code file saved successfully.");
    }

    @GetMapping("/getFileList/{userId}")
    public List<DscCodeFile> getFileList(@PathVariable String userId) {
        return dscCodeFileService.getFileList(userId);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResult<String> deleteCodeFile(@PathVariable String id) {
        // 调用 Service 层删除文件
        boolean isDeleted = dscCodeFileService.deleteCodeFile(id);

        if (isDeleted) {
            return CommonResult.success("Code file deleted successfully.");
        } else {
            return CommonResult.failed("Code file not found.");
        }
    }

    @PostMapping("/rename")
    public CommonResult<String> renameCodeFile(@RequestParam String id, @RequestParam String newFileName) {
        boolean success = dscCodeFileService.renameFileName(id, newFileName);
        if (success) {
            return CommonResult.success("File name updated successfully.");
        } else {
            return CommonResult.failed("File not found or update failed.");
        }
    }

}

