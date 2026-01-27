package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
import nnu.wyz.systemMS.model.dto.ConvertSgrd2GeoTIFFDTO;

import java.util.List;

public interface DscGeoAnalysisToolService {

    CommonResult<?> getGeoAnalysisTool(String toolId);


    CommonResult<String> convertSgrd2Geotiff(ConvertSgrd2GeoTIFFDTO convertSgrd2GeoTIFFDTO);

    CommonResult<List<JSONObject>> getGeoAnalysisToolList();
    CommonResult<?> getToolCategory();
}
