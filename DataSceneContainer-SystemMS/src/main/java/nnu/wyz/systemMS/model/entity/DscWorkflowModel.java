package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * @author tjk
 * @date 2025/1/2
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "DscWorkflowModel对象", description = "工作流集成模型描述对象")
public class DscWorkflowModel {
    @Id
    @ApiModelProperty(value = "模型ID")
    private String id;

    @ApiModelProperty(value = "模型名称")
    private String name;

    @ApiModelProperty(value = "模型JSON内容")
    private String modelJson;

    @ApiModelProperty(value = "模型所有者ID")
    private String userId;

    @ApiModelProperty(value = "模型创建时间")
    private String createTime;

    @ApiModelProperty(value = "模型更新时间")
    private String updateTime;

}
