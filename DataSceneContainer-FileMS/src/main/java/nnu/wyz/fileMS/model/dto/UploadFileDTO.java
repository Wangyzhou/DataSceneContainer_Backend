package nnu.wyz.fileMS.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 16:22
 */
@Data
@ApiModel(value = "文件上传DTO")
@Accessors(chain = true)
public class UploadFileDTO {

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "文件上传Id")
    private String taskId;

    @ApiModelProperty(value = "目录ID")
    private String catalogId;
}
