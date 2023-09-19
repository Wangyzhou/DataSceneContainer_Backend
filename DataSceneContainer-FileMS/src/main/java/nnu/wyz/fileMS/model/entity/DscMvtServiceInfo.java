package nnu.wyz.fileMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author yzwang
 * @since 2023-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="DscMvtServiceInfo对象", description="")
public class DscMvtServiceInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ApiModelProperty(value = "唯一标识")
    private String id;

    @ApiModelProperty(value = "文件ID，预留")
    private String fileId;

    @ApiModelProperty(value = "mvt服务名称")
    private String mvtName;

    @ApiModelProperty(value = "mvt服务链接")
    private String mvtUrl;

    @ApiModelProperty(value = "服务类型")
    private String type;

    @ApiModelProperty(value = "几何类型")
    private String geoType;

    @ApiModelProperty(value = "几何中心")
    private List<Double> center;

    @ApiModelProperty(value = "几何中心")
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
