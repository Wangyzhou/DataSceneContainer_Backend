package nnu.wyz.systemMS.model.entity;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/4 21:16
 */
@Data
@ApiModel(value = "DscGeoTools", description = "地理工具")
public class DscGeoTools {

    @ApiModelProperty(value = "唯一标识")
    private String id;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "类型")
    private String type;

    @ApiModelProperty(value = "运行参数")
    private JSONObject parameter;

    @ApiModelProperty(value = "python函数")
    private String python_function;

    @ApiModelProperty(value = "源码地址")
    private String source_code;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "创建时间")
    private String created_time;

    @ApiModelProperty(value = "最后修改时间")
    private String last_modified_time;

}
