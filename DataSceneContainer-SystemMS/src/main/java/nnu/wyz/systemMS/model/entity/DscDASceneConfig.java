package nnu.wyz.systemMS.model.entity;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/1/6 17:46
 */

@Data
@Accessors(chain = true)
public class DscDASceneConfig extends DscGDVSceneConfig{

    @ApiModelProperty(value = "场景数据根目录ID")
    private String sceneDataRootCatalogId;

    @ApiModelProperty(value = "场景工具调用日志")
    private List<JSONObject> toolLogs;

}
