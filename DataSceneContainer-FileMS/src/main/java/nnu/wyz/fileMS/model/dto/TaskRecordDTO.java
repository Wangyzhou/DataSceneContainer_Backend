package nnu.wyz.fileMS.model.dto;

import cn.hutool.core.bean.BeanUtil;
import com.amazonaws.services.s3.model.PartSummary;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import nnu.wyz.fileMS.model.entity.SysUploadTask;

import java.util.List;

@Data
@ToString
@Accessors(chain = true)
@ApiModel(value = "文件上传任务记录DTO")
public class TaskRecordDTO extends SysUploadTask {

    /**
     * 已上传完的分片
     */
    @ApiModelProperty(value = "已上传的文件分片")
    private List<PartSummary> exitPartList;


    public static TaskRecordDTO convertFromEntity (SysUploadTask task) {
        TaskRecordDTO dto = new TaskRecordDTO();
        BeanUtil.copyProperties(task, dto);
        return dto;
    }
}
