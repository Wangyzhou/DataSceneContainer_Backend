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

    private String type;

    private String author;

    private String identifier;

    private String library;

    private String version;

    private String description;

    private List<String> references;

    private DscGeoAnalysisToolDesParams parameters;

}
