package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/7/30 18:17
 */

@Data
@Accessors(chain = true)
@ApiModel(value = "栅格服务内部引用子服务实体")
@AllArgsConstructor
@NoArgsConstructor
public class RasterSRef {

    @ApiModelProperty(value = "子服务关联的场景id")
    private String sceneId;

    @ApiModelProperty(value = "子服务的文件id(png)")
    private String fileId;

    @ApiModelProperty(value = "子服务的url")
    private String url;
}
