package nnu.wyz.systemMS.utils;

import java.util.HashMap;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/6 16:16
 */

public class GeoAnalysisOutputMap {

    private static final String Shapes = "Shapes (output)";

    private static final String Grids = "Grid (output)";

    private static final String Tables = "Table (output)";

    private static  HashMap<String, String> typeSuffixMapping;

    static {
        typeSuffixMapping = new HashMap<>();
        typeSuffixMapping.put(Shapes, ".shp");
        typeSuffixMapping.put(Grids, ".tif");
        typeSuffixMapping.put(Tables, ".csv");
    }

}
