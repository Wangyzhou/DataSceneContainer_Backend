package nnu.wyz.systemMS.model.entity.codeModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoActTask {
    private String taskId;
    private String parentPath;
    private String fileName;
    private Date startTime;
    private Date endTime;
    private String status;
}
