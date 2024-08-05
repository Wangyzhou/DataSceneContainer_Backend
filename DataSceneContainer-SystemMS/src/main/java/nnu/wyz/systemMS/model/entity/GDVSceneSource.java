package nnu.wyz.systemMS.model.entity;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/19 20:16
 */
@Data
public class GDVSceneSource {

    private String sourceId;

    private String sourceName;

    private String sourceType;

    private String url;

    private String user;

    // 矢量（点线面）
    private String geoType;

    // 栅格（tif/png等）
    private String fileType;

    private String ptName;

    private List<Double> bbox;

    private JSONObject clusterOptions;

}
