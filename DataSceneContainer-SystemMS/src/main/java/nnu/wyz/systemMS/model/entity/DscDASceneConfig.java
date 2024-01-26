package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/1/6 17:46
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class DscDASceneConfig extends DscGDVSceneConfig{

    @ApiModelProperty(value = "场景数据根目录ID")
    private String sceneDataRootCatalogId;

    @ApiModelProperty(value = "场景日志")
    private List<DASSceneLog> sceneLogs;

}
