package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/2 14:58
 */
@Data
@Accessors(chain = true)
public class DscUserRasterS {
    @Id
    @ApiModelProperty(value = "唯一标识")
    private String id;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "栅格服务ID")
    private String rasterSId;

    @ApiModelProperty(value = "矢量服务名称")
    private String rasterSName;

    @ApiModelProperty(value = "矢量服务类型")
    private String rasterSType;

}
