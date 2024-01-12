package nnu.wyz.systemMS.service.iml;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscGeoAnalysisDAO;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
import nnu.wyz.systemMS.service.DscGeoAnalysisToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/11 16:49
 */
@Service
public class DscGeoAnalysisToolServiceIml implements DscGeoAnalysisToolService {

    @Autowired
    private DscGeoAnalysisDAO dscGeoAnalysisDAO;
    @Override
    public CommonResult<DscGeoAnalysisTool> getGeoAnalysisTool(String toolId) {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(toolId);
        return byId.map(dscGeoAnalysisTool -> CommonResult.success(dscGeoAnalysisTool, "获取工具成功")).orElseGet(() -> CommonResult.failed("未找到该工具"));
    }
}
