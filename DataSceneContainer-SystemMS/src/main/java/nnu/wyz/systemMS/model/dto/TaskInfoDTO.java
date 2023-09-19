package nnu.wyz.systemMS.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "文件上传任务信息DTO")
public class TaskInfoDTO {

    /**
     * 是否完成上传（是否已经合并分片）
     */
    @ApiModelProperty(value = "文件分片上传是否完成")
    private boolean finished;

    /**
     * 文件地址
     */
    @ApiModelProperty(value = "文件存储地址")
    private String path;

    /**
     * 上传记录
     */
    @ApiModelProperty(value = "任务记录")
    private TaskRecordDTO taskRecord;

}
