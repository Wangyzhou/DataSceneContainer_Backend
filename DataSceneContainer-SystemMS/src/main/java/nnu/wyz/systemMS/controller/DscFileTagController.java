package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.FileTagDTO;
import nnu.wyz.systemMS.model.dto.UpdateFileTagDTO;
import nnu.wyz.systemMS.service.DscFileTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/18 20:05
 */

@RestController
@RequestMapping(value = "/dsc-file-tag")
@Api(value = "DscFileTagController", tags = "文件标签接口")
public class DscFileTagController {

    @Autowired
    private DscFileTagService dscFileTagService;

    @ApiOperation(value = "获取某个文件标签")
    @GetMapping(value = "/{fileId}")
    public CommonResult<FileTagDTO> getFileTag(@PathVariable("fileId") String fileId) {
        return dscFileTagService.getFileTag(fileId);
    }

    @ApiOperation(value = "更新文件标签")
    @PostMapping(value = "/{userId}/{fileId}")
    public CommonResult<String> updateFileTag(@PathVariable("userId") String userId, @PathVariable("fileId") String fileId, @RequestBody UpdateFileTagDTO updateFileTagDTO) {
        return dscFileTagService.updateFileTag(userId, fileId, updateFileTagDTO);
    }

    @ApiOperation(value = "删除文件标签")
    @DeleteMapping(value = "/{fileId}")
    public CommonResult<String> deleteFileTag(@PathVariable("fileId") String fileId) {
        return dscFileTagService.deleteFileTag(fileId);
    }

    @ApiOperation(value = "获取所有文件标签")
    @GetMapping(value = "/all/{userId}")
    public CommonResult<List<FileTagDTO>> getAllFileTag(@PathVariable("userId") String userId) {
        return dscFileTagService.getAllFileTag(userId);
    }
}
