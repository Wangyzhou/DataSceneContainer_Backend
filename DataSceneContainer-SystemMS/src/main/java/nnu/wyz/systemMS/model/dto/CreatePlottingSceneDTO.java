package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author tjk
 * @date 2025/8/1
 * @Description
 */
@Data
@Accessors(chain = true)
public class CreatePlottingSceneDTO {
    @ApiModelProperty("场景名称")
    private String name;

    @ApiModelProperty("SML文件字符串")
    private String smlString;

    @ApiModelProperty("创建用户")
    private String createdUser;

    @ApiModelProperty("缩略图")
    private MultipartFile thumbnail;
}
