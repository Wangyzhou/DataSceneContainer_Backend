package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 10:43
 */
@Data
@ApiModel(value = "目录创建DTO")
public class CreateCatalogDTO {

    /**
     * 目录名
     */
    @ApiModelProperty(value = "目录名")
    private String catalogName;

    /**
     * 用户ID
     */
    @ApiModelProperty(value = "用户ID")
    private String userId;

    /**
     * 父目录ID
     */
    @ApiModelProperty(value = "父目录ID")
    private String parentCatalogId;

}
