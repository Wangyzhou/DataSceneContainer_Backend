package nnu.wyz.systemMS.model.DscGeoAnalysis;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 14:46
 */
@Data
public class DscGeoAnalysisTool {

    private String id;

    private String name;

    private String libirary;

    private Integer identifier;

    private String author;

    private String description;

    private String references;

    private List<DscGeoAnalysisToolParams> input;

    private List<DscGeoAnalysisToolParams> output;

    private List<DscGeoAnalysisToolParams> options;

}
