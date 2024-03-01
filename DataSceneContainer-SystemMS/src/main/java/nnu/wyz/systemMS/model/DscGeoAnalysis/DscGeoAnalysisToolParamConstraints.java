package nnu.wyz.systemMS.model.DscGeoAnalysis;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 14:53
 */
@Data
public class DscGeoAnalysisToolParamConstraints {

    private Double minimum;

    private Double maximum;

    private JSONObject choices;

    private String defaultValue;

}
