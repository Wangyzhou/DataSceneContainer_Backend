package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author tjk
 * @date 2025/8/1
 * @Description
 */
@ApiModel(value = "标绘场景实体")
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class DscPlottingScene {
    @Id
    @ApiModelProperty(value = "标识")
    private String id;

    @ApiModelProperty(value = "场景名称")
    private String name;

    @ApiModelProperty(value = "SML文件字符串")
    private String smlString;

    @ApiModelProperty(value = "缩略图URL")
    private String thumbnail;

    @ApiModelProperty(value = "创建用户")
    private String createdUser;

    @ApiModelProperty(value = "创建时间")
    private String createdTime;

    @ApiModelProperty(value = "更新时间")
    private String updatedTime;
}

