package nnu.wyz.systemMS.model.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.internal.filter.ValueNodes;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

@Data
@Accessors(chain = true)
@ApiModel(value = "战例底图")
@AllArgsConstructor
@NoArgsConstructor
public class DscMap {
    @Id
    @ApiModelProperty(value = "标识")
    private String id;

    @ApiModelProperty(value = "地图名称")
    private String name;

    @ApiModelProperty(value = "地图样式")
    private JsonNode mapStyle;

    @ApiModelProperty(value = "地图发布链接")
    private String mapUrl;

    @ApiModelProperty(value = "用户标识")
    private String userId;

    @ApiModelProperty(value = "创建日期")
    private String publishTime;

    @ApiModelProperty(value = "缩略图")
    private String thumbnail;

    @ApiModelProperty(value = "简介")
    private String introduction;
}
