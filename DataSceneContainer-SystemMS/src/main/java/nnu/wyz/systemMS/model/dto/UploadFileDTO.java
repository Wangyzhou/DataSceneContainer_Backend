package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 16:22
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "文件上传DTO")
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileDTO {

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "上传任务ID")
    private String taskId;

    @ApiModelProperty(value = "目录ID")
    private String catalogId;

}
