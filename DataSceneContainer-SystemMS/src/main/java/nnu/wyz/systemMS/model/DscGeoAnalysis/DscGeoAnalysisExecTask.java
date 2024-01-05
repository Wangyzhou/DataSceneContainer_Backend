package nnu.wyz.systemMS.model.DscGeoAnalysis;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/13 15:10
 */

@Data
@Accessors(chain = true)
public class DscGeoAnalysisExecTask {

    @Id
    private String id;

    private String executor;

    private String targetTool;

    private DscGARawParams params;

    private int status;     //1 成功  0 进行中  -1 失败  -2 已取消

    private String startTime;

    private String endTime;

    private String description;

}