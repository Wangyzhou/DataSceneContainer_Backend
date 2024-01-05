package nnu.wyz.systemMS.model.DscGeoAnalysis;

import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 14:49
 */
@Data
public class DscGeoAnalysisToolParams {

    private String name;

    private String flag;

    private String type;

    private String description;

    private DscGeoAnalysisToolParamConstraints constraints;

    private Object defaultValue;

}
