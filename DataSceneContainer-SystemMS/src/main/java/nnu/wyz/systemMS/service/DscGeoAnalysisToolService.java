package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;

public interface DscGeoAnalysisToolService {

    CommonResult<DscGeoAnalysisTool> getGeoAnalysisTool(String toolId);


}
