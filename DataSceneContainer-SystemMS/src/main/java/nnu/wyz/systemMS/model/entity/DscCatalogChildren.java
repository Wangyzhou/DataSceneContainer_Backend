package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

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
@ApiModel(value="DscCatalogChildren对象", description="")
public class DscCatalogChildren implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    @ApiModelProperty(value = "文件夹或资源")
    private String type;

    private String catalogId;

    private String updatedTime;


}
