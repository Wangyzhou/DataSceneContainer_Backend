package nnu.wyz.systemMS.model.entity.codeModel;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@ApiModel(value = "Model对象", description = "工作流集成模型与自定义封装描述对象")
public class DscCodeModelTaskTempPaths {
    @Id
    @ApiModelProperty(value = "ID")
    private String id;

    @ApiModelProperty(value = "任务ID")
    private String taskId;

    @ApiModelProperty(value = "临时目录")
    private List<String> tempPaths;

    @ApiModelProperty(value = "状态")
    private String status;

    public DscCodeModelTaskTempPaths(){
        this.tempPaths = new ArrayList<>();
        this.status = "todo";
    }
}
