package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/12 20:22
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "场景实体")
@AllArgsConstructor
public class DscScene {

    @Id
    @ApiModelProperty(value = "标识")
    private String id;

    @ApiModelProperty(value = "场景名称")
    private String name;

    @ApiModelProperty(value = "场景类型")
    private String type;

    @ApiModelProperty(value = "缩略图")
    private String thumbnail;

    @ApiModelProperty(value = "创建用户")
    private String createdUser;

    @ApiModelProperty(value = "编辑计数")
    private Long editCount;

    @ApiModelProperty(value = "创建日期")
    private String createdTime;

    @ApiModelProperty(value = "更新日期")
    private String updatedTime;

    @ApiModelProperty(value = "是否锁定")
    private Boolean isLocked;

    @ApiModelProperty(value = "权限范围")
    private Integer permissionRange;

}
