package nnu.wyz.fileMS.service;

import com.alibaba.nacos.api.model.v2.Result;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.fileMS.model.dto.TaskInfoDTO;
import nnu.wyz.fileMS.model.dto.UploadFileDTO;
import nnu.wyz.fileMS.model.param.InitTaskParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@FeignClient(value = "dsc-systemMS")
@Component
public interface DscRemoteFileMS {

    @PostMapping("/dsc-file-uploader")
    Result<TaskInfoDTO> initTask(@Valid @RequestBody InitTaskParam param);

    @PostMapping("/dsc-file")
    CommonResult<String> upload(@RequestBody UploadFileDTO uploadFileDTO);
}
