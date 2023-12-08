package nnu.wyz.systemMS.service.iml;

import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscGeoToolsDAO;
import nnu.wyz.systemMS.model.entity.DscGeoTools;
import nnu.wyz.systemMS.service.DscGeoToolsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/5 9:47
 */
@Service
@Slf4j
public class DscGeoToolsServiceIml implements DscGeoToolsService {

    @Autowired
    private DscGeoToolsDAO dscGeoToolsDAO;
    @Override
    public CommonResult<DscGeoTools> getGeoToolInfoById(String id) {
        Optional<DscGeoTools> byId = dscGeoToolsDAO.findById(id);
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该工具");
        }
        DscGeoTools dscGeoTool = byId.get();
        return CommonResult.success(dscGeoTool, "获取工具成功!");
    }
}
