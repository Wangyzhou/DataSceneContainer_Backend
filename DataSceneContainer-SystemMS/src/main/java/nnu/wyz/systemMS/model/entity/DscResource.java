package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author yzwang
 * @since 2023-08-25
 */
@Data
@Accessors(chain = true)
@ApiModel(value="DscResource对象", description="")
public class DscResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "资源唯一标识")
    @Id
    private String id;

    @ApiModelProperty(value = "资源名")
    private String name;

    @ApiModelProperty(value = "资源类型: file、tool、etc...")
    private String type;

    @ApiModelProperty(value = "创建时间")
    private String createdTime;

    @ApiModelProperty(value = "最后更新时间")
    private String updatedTime;

    @ApiModelProperty(value = "查看计数")
    private Long watchCount;

    @ApiModelProperty(value = "所有者")
    private String createdUser;

    @ApiModelProperty(value = "资源描述,最多200字符")
    private String description;

    @ApiModelProperty(value = "资源拥有用户个数，为0则删除该资源")
    private Long ownerCount;


}
