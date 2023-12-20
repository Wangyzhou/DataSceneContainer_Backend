package nnu.wyz.systemMS.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import nnu.wyz.systemMS.model.param.DscGeoToolExecParams;
import nnu.wyz.systemMS.model.param.DscToolRawParams;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/13 15:10
 */

@Data
@Accessors(chain = true)
public class DscGeoToolExecTask {

    @Id
    private String id;

    private String executor;

    private String targetTool;

    private List<DscToolRawParams> params;

    private int status;     //1 成功  0 进行中  -1 失败  -2 已取消

    private String startTime;

    private String endTime;

    private String description;

}
