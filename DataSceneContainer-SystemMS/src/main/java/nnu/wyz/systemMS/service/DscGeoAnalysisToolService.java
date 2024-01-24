package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
import nnu.wyz.systemMS.model.dto.ConvertSgrd2GeoTIFFDTO;

public interface DscGeoAnalysisToolService {

    CommonResult<DscGeoAnalysisTool> getGeoAnalysisTool(String toolId);


    CommonResult<String> convertSgrd2Geotiff(ConvertSgrd2GeoTIFFDTO convertSgrd2GeoTIFFDTO);

}
