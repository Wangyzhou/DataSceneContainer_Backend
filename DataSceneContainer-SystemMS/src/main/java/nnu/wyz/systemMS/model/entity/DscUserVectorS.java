package nnu.wyz.systemMS.model.entity;

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
public class DscUserVectorS {

    @Id
    @ApiModelProperty(value = "唯一标识")
    private String id;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "矢量服务ID")
    private String vectorSId;

    @ApiModelProperty(value = "矢量服务名称")
    private String vectorSName;

    @ApiModelProperty(value = "矢量服务类型")
    private String vectorSType;

}
