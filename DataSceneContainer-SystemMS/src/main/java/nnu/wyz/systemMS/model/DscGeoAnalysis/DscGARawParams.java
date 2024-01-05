package nnu.wyz.systemMS.model.DscGeoAnalysis;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 14:08
 */
@Data
public class DscGARawParams {

    private String workingDir;

    private List<DscGAInvokeInnerParams> input;

//    private List<DscGAInvokeInnerParams> output;

    private List<DscGAInvokeInnerParams> options;

}
