package nnu.wyz.fileMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/7 22:01
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "用户mvt服务")
public class DscUserMvtS {

    @Id
    @ApiModelProperty(value = "唯一标识")
    private String id;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "mvt服务ID")
    private String mvtId;

    @ApiModelProperty(value = "mvt服务名称")
    private String mvtSName;

}
