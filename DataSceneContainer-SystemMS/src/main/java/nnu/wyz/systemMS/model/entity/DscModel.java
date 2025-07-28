package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @author tjk
 * @date 2025/1/2
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "Model对象", description = "工作流集成模型与自定义封装描述对象")
public class DscModel {
    @Id
    @ApiModelProperty(value = "模型ID")
    private String id;

    @ApiModelProperty(value = "模型名称")
    private String name;

    @ApiModelProperty(value = "模型所属类别")
    private String category;

    @ApiModelProperty(value = "模型描述")
    private String description;

    @ApiModelProperty(value = "模型参数")
    private DscModelParams params;

    @ApiModelProperty(value = "模型JSON内容")
    private String modelJson;

    @ApiModelProperty(value = "模型作者")
    private String author;

    @ApiModelProperty(value = "模型拥有者ID")
//    private List<String> ownerId;   当需要模型共享时再启用
    private String ownerId;

    @ApiModelProperty(value = "模型创建时间")
    private String createTime;

    @ApiModelProperty(value = "模型更新时间")
    private String updateTime;

}
