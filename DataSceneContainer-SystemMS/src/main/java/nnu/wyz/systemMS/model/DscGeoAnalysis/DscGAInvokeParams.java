package nnu.wyz.systemMS.model.DscGeoAnalysis;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 10:05
 */
@Data
public class DscGAInvokeParams {

    private String toolId;

    private String executor;

    private String sceneCatalog;

    private Map<String, String> input;

    private Map<String, Object> options;


}
