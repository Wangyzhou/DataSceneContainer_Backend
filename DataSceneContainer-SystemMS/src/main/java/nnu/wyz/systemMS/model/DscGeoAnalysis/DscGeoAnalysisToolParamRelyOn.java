package nnu.wyz.systemMS.model.DscGeoAnalysis;

import lombok.Data;
import org.json.JSONObject;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/3/5 14:40
 */
@Data
public class DscGeoAnalysisToolParamRelyOn {

    private String relyId;

    private List<JSONObject> config;

}
