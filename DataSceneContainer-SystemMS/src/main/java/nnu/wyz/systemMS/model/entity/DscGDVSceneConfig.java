package nnu.wyz.systemMS.model.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/14 14:27
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "地理可视化场景配置项")
public class DscGDVSceneConfig {

    @Id
    @ApiModelProperty(value = "唯一ID")
    private String id;

    @ApiModelProperty(value = "所属场景ID")
    private String sceneId;

    @ApiModelProperty(value = "场景数据源")
    private List<JSONObject> sources;

    @ApiModelProperty(value = "场景图层")
    private List<JSONObject> layers;

    @ApiModelProperty(value = "场景相机姿态")
    private JSONObject pos;

    @ApiModelProperty(value = "地图相关参数")
    private JSONObject mapParams;
}
