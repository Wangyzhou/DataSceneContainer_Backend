package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PublishImageDTO;
import nnu.wyz.systemMS.model.entity.DscRasterService;

import java.util.List;

public interface DscRasterSService {

    CommonResult<String> publishImage2RasterS(PublishImageDTO publishImageDTO);

    CommonResult<List<DscRasterService>> getRasterServiceList(String userId);

    CommonResult<String> deleteRasterService(String userId, String rasterSId);
}
