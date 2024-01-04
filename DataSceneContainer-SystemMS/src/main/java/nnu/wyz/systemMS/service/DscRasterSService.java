package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.PublishImageDTO;
import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.PageInfo;

import java.util.List;

public interface DscRasterSService {

    CommonResult<String> publishImage2RasterS(PublishImageDTO publishImageDTO);

    CommonResult<PageInfo<DscRasterService>> getRasterServiceList(PageableDTO pageableDTO);

    CommonResult<String> deleteRasterService(String userId, String rasterSId);

    CommonResult<List<DscRasterService>> getRasterServiceListByFileId(String fileId);

}
