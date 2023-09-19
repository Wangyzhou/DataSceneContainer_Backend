package nnu.wyz.resourceMS.controller;

import io.swagger.annotations.Api;
import nnu.wyz.resourceMS.model.dto.Result;
import nnu.wyz.resourceMS.model.dto.TaskInfoDTO;
import nnu.wyz.resourceMS.model.entity.SysUploadTask;
import nnu.wyz.resourceMS.model.param.InitTaskParam;
import nnu.wyz.resourceMS.service.SysUploadTaskService;
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
     * @param identifier 文件md5
     * @return
     */
    @GetMapping("/{identifier}")
    public Result<TaskInfoDTO> taskInfo (@PathVariable("identifier") String identifier) {
        return Result.ok(sysUploadTaskService.getTaskInfo(identifier));
    }

    /**
     * 创建一个上传任务
     * @return
     */
    @PostMapping
    public Result<TaskInfoDTO> initTask (@Valid @RequestBody InitTaskParam param, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return Result.error(bindingResult.getFieldError().getDefaultMessage());
        }
        return Result.ok(sysUploadTaskService.initTask(param));
    }

    /**
     * 获取每个分片的预签名上传地址
     * @param identifier
     * @param partNumber
     * @return
     */
    @GetMapping("/{identifier}/{partNumber}")
    public Result preSignUploadUrl (@PathVariable("identifier") String identifier, @PathVariable("partNumber") Integer partNumber) {
        SysUploadTask task = sysUploadTaskService.getByIdentifier(identifier);
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
     * @param identifier
     * @return
     */
    @PostMapping("/merge/{identifier}")
    public Result merge (@PathVariable("identifier") String identifier) {
        sysUploadTaskService.merge(identifier);
        return Result.ok();
    }
    @GetMapping("/download/{identifier}")
    public void download(@PathVariable("identifier")String md5, HttpServletResponse response){
        sysUploadTaskService.downlod(md5, response);
    }
}
