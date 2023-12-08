package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.entity.DscGeoTools;

public interface DscGeoToolsService {

    CommonResult<DscGeoTools> getGeoToolInfoById(String id);
}
