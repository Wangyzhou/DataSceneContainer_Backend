package nnu.wyz.systemMS.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ToString
@Accessors(chain = true)
@ApiModel(value = "初始化文件上传请求参数")
public class InitTaskParam {

    /**
     * 文件唯一标识(MD5)
     */
    @ApiModelProperty(value = "文件MD5")
    @NotBlank(message = "文件标识不能为空")
    private String identifier;

    /**
     * 文件大小（byte）
     */
    @ApiModelProperty(value = "文件大小")
    @NotNull(message = "文件大小不能为空")
    private Long totalSize;

    /**
     * 分片大小（byte）
     */
    @ApiModelProperty(value = "文件分片大小")
    @NotNull(message = "分片大小不能为空")
    private Long chunkSize;

    /**
     * 文件名称
     */
    @ApiModelProperty(value = "文件名称")
    @NotBlank(message = "文件名称不能为空")
    private String fileName;

    @ApiModelProperty(value = "对象Id")
    private String objectName;

    /**
     * 上传用户
     */
    @ApiModelProperty(value = "上传用户")
    @NotBlank(message = "上传用户不能为空")
    private String userId;

    private String fileId;
}
