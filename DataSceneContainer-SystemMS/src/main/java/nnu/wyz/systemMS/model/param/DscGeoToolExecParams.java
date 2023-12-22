package nnu.wyz.systemMS.model.param;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/13 15:47
 */
@Data
public class DscGeoToolExecParams {

    private String taskId;

    private String toolId;

    private List<DscToolRawParams> params;
}
