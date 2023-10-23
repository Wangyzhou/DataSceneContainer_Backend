package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/11 14:57
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "矢量服务")
public class DscVectorServiceInfo {

    @ApiModelProperty(value = "唯一标识")
    private String id;

    @ApiModelProperty(value = "发布文件Id")
    private String fileId;

    @ApiModelProperty(value = "矢量服务名")
    private String name;

    @ApiModelProperty(value = "矢量服务类型")
    private String type;

    @ApiModelProperty(value = "矢量服务url")
    private String url;

    @ApiModelProperty(value = "几何类型")
    private String geoType;

    @ApiModelProperty(value = "几何中心")
    private List<Double> center;

    @ApiModelProperty(value = "几何范围")
    private List<Double> bbox;

    @ApiModelProperty(value = "pg表名")
    private String ptName;

    @ApiModelProperty(value = "发布者")
    private String publisher;

    @ApiModelProperty(value = "服务拥有者计数")
    private Long ownerCount;

    @ApiModelProperty(value = "发布时间")
    private String publishTime;
}
