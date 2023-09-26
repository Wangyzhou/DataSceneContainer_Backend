package nnu.wyz.systemMS.model.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import nnu.wyz.systemMS.model.entity.GDVSceneSource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/12 20:49
 */
@Data
@ApiModel(value = "地理数据可视化场景创建DTO")
@AllArgsConstructor
public class SaveGDVSceneDTO {

    @ApiModelProperty(value = "创建用户")
    private String userId;

    @ApiModelProperty(value = "场景Id")
    private String sceneId;

    @ApiModelProperty(value = "场景名称")
    private String name;

    @ApiModelProperty(value = "场景缩略图")
    private MultipartFile thumbnail;

    @ApiModelProperty(value = "场景数据源")
    private List<GDVSceneSource> sources;

    @ApiModelProperty(value = "场景图层")
    private List<JSONObject> layers;

    @ApiModelProperty(value = "场景姿态")
    private JSONObject pos;

    @ApiModelProperty(value = "场景地图参数")
    private MapParamsDTO mapParams;
}
