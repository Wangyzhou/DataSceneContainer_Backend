package nnu.wyz.systemMS.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nnu.wyz.systemMS.model.dto.Result;
import nnu.wyz.systemMS.model.dto.TaskInfoDTO;
import nnu.wyz.systemMS.model.entity.SysUploadTask;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.service.SysUploadTaskService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/25 10:16
 */
@RestController
@RequestMapping("/dsc-file-uploader")
@Api(value = "DscResourceController", tags = "文件分片上传接口，与系统文件数据库信息隔离")
public class DscFileMinIOUploaderController {

    @Resource
    private SysUploadTaskService sysUploadTaskService;


    /**
     * 获取文件上传进度
     *
     * @param identifier 文件md5
     * @return
     */
    @ApiOperation(value = "获取文件上传信息")
    @GetMapping("/{userId}/{identifier}")
    public Result<TaskInfoDTO> taskInfo(@PathVariable("userId") String userId, @PathVariable("identifier") String identifier) {
        return Result.ok(sysUploadTaskService.getTaskInfo(userId, identifier));
    }

    /**
     * 创建一个上传任务
     *
     * @return
     */
    @ApiOperation(value = "创建一个文件上传任务")
    @PostMapping
    public Result<TaskInfoDTO> initTask(@Valid @RequestBody InitTaskParam param, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        return Result.ok(sysUploadTaskService.initTask(param));
    }

    /**
     * 获取每个分片的预签名上传地址
     *
     * @param identifier
     * @param partNumber
     * @return
     */
    @ApiOperation(value = "获取每个分片的预签名上传地址")
    @GetMapping("/{userId}/{identifier}/{partNumber}")
    public Result preSignUploadUrl(@PathVariable("userId") String userId, @PathVariable("identifier") String identifier, @PathVariable("partNumber") Integer partNumber) {
        SysUploadTask task = sysUploadTaskService.getByUploaderAndMd5(userId, identifier);
        if (task == null) {
            return Result.error("分片任务不存在");
        }
        Map<String, String> params = new HashMap<>();
        params.put("partNumber", partNumber.toString());
        params.put("uploadId", task.getUploadId());
        return Result.ok(sysUploadTaskService.genPreSignUploadUrl(task.getBucketName(), task.getObjectKey(), params));
    }

    /**
     * 合并分片
     *
     * @param identifier
     * @return
     */
    @ApiOperation(value = "合并分片")
    @PostMapping("/merge/{userId}/{identifier}")
    public Result merge(@PathVariable("userId") String userId, @PathVariable("identifier") String identifier) {
        return sysUploadTaskService.merge(userId, identifier);
    }
}
