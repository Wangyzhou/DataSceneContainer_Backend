package nnu.wyz.systemMS.model.DscGeoAnalysis;

import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/22 9:57
 */

@Data
public class DscGeoAnalysisToolInnerParams {

    private String paramId;

    private String name;

    private String type;

    private String category;

    private String identifier;

    private String description;

    private Boolean isOptional;

    private DscGeoAnalysisToolParamConstraints constraints;

    private DscGeoAnalysisToolParamRelyOn relyOn;

}
