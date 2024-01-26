package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PublishImageDTO;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/1 21:55
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "栅格服务实体")
public class DscRasterService {

    @Id
    @ApiModelProperty(value = "唯一Id")
    private String id;

    @ApiModelProperty(value = "栅格服务名称")
    private String name;

    @ApiModelProperty(value = "栅格服务类型")
    private String type;

    @ApiModelProperty(value = "栅格服务url")
    private String url;

    @ApiModelProperty(value = "发布文件Id")
    private String fileId;

    @ApiModelProperty(value = "原始文件Id（可选）")
    private String oriFileId;

    @ApiModelProperty(value = "空间范围")
    private List<Double> bbox;

    @ApiModelProperty(value = "发布者")
    private String publisher;

    @ApiModelProperty(value = "服务拥有者计数")
    private Long ownerCount;

    @ApiModelProperty(value = "发布时间")
    private String publishTime;

}
