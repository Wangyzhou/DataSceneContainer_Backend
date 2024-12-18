package nnu.wyz.systemMS.model.DscGeoAnalysis;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/5 14:08
 */
@Data
public class DscGARawParams {

    private String workingDir;

    private Map<String, String> input;

    private Map<String, String> output;

    private Map<String, Object> options;

    private List<JSONObject> outputs;

}
