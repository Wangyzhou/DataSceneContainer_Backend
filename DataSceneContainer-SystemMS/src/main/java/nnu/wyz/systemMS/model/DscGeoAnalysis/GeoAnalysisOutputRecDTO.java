package nnu.wyz.systemMS.model.DscGeoAnalysis;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/8 10:57
 */

@Data
@AllArgsConstructor
public class GeoAnalysisOutputRecDTO {

    private String physicalNameWithoutSuffix;

    private String fileNameWithoutSuffix;
}
