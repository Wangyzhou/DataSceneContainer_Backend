package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/31 15:41
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "文件删除DTO")
public class DeleteFileDTO {

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "文件ID")
    private String fileId;

    @ApiModelProperty(value = "目录ID")
    private String catalogId;

}
