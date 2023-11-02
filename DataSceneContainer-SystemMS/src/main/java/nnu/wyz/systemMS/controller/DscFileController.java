package nnu.wyz.systemMS.controller;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.service.DscFileService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 16:02
 */
@RestController
@RequestMapping(value = "/dsc-file")
@Api(value = "DscFileController", tags = "文件管理接口")
public class DscFileController {

    private final DscFileService dscFileService;


    public DscFileController(DscFileService dscFileService) {
        this.dscFileService = dscFileService;
    }

    @ApiOperation(value = "文件上传(创建文件记录、开通用户权限)")
    @PostMapping
    public CommonResult<String> upload(@RequestBody UploadFileDTO uploadFileDTO) {
        return dscFileService.create(uploadFileDTO);
    }

    @ApiOperation(value = "文件删除")
    @DeleteMapping
    public CommonResult<String> delete(@RequestBody DeleteFileDTO deleteFileDTO) {
        return dscFileService.delete(deleteFileDTO);
    }

    @ApiOperation(value = "文件下载")
    @GetMapping("/download/{fileId}")
    public void download(@PathVariable("fileId") String fileId, HttpServletResponse response) {
        dscFileService.download(fileId, response);
    }

    @ApiOperation(value = "获取文件详细信息")
    @GetMapping("/getFileInfo/{fileId}")
    public CommonResult<JSONObject> getFileInfo(@PathVariable("fileId") String fileId) {
        return dscFileService.getFileInfo(fileId);
    }

    @ApiOperation(value = "获取文件预览url")
    @GetMapping("/getFilePreviewUrl/{fileId}")
    public CommonResult<String> getFilePreviewUrl(@PathVariable("fileId") String fileId) {
        return dscFileService.getFilePreviewUrl(fileId);
    }

    @ApiOperation(value = "文件分享")
    @PostMapping(value = "/share")
    public CommonResult<String> shareFile(@RequestBody FileShareDTO fileShareDTO) {
        return dscFileService.shareFile(fileShareDTO);
    }


    @ApiOperation(value = "zip解压")
    @PostMapping(value = "/unzip")
    public CommonResult<String> unzip(@RequestBody UnzipFileDTO unzipFileDTO) {
        return dscFileService.unzip(unzipFileDTO);
    }



}
