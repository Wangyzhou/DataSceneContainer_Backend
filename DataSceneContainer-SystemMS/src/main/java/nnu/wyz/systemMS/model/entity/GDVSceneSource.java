package nnu.wyz.systemMS.model.entity;

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

    private String serviceSource;

    private String url;

    private String user;

    private String geoType;

    private String ptName;

    private Boolean isRenaming;

    private List<Double> bbox;
}
