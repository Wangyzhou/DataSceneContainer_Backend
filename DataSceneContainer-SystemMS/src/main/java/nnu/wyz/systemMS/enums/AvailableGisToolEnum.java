package nnu.wyz.systemMS.enums;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/2/29 15:54
 */

public enum AvailableGisToolEnum {

    SAGA_GIS_TOOL(1, "saga_gis"),
    GRASS_GIS_TOOL(2, "grass_gis"),
    DIY_GIS_TOOL(3, "diy_gis");


    /**
     * 工具代码
     */
    private Integer code;

    /**
     * 工具名称
     */
    private String name;

    AvailableGisToolEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AvailableGisToolEnum fromCode(Integer code) {
        for (AvailableGisToolEnum tool : AvailableGisToolEnum.values()) {
            if (tool.getCode().equals(code)) {
                return tool;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }
}
