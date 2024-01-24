package nnu.wyz.systemMS.model.DscGeoAnalysis;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/22 9:57
 */
@Data
public class DscGeoAnalysisToolDesParams {

    private List<DscGeoAnalysisToolInnerParams> inputs;

    private List<DscGeoAnalysisToolInnerParams> outputs;

    private List<DscGeoAnalysisToolInnerParams> options;

}

