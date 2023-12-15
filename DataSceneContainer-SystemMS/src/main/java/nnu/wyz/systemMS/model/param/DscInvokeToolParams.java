package nnu.wyz.systemMS.model.param;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/13 9:54
 */
@Data
public class DscInvokeToolParams {

    private String toolId;

    private String userId;

    private List<DscToolRawParams> toolRawParams;


}
