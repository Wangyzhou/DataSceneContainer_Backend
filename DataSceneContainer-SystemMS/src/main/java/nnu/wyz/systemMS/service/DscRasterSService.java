package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.PublishImageDTO;
import nnu.wyz.systemMS.model.dto.PublishTiff2ImageDTO;
import nnu.wyz.systemMS.model.dto.PublishTiff2TMSDTO;
import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.PageInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface DscRasterSService {

    CommonResult<String> publishImage2RasterS(PublishImageDTO publishImageDTO);

    CommonResult<String> publishTiff2RasterS(PublishTiff2ImageDTO publishTiff2ImageDTO);

    CommonResult<String> publishTiff2TMS(PublishTiff2TMSDTO publishTiff2TMSDTO);

    CommonResult<PageInfo<DscRasterService>> getRasterServiceList(PageableDTO pageableDTO);

    CommonResult<String> deleteRasterService(String userId, String rasterSId);

    CommonResult<List<DscRasterService>> getRasterServiceListByFileId(String fileId);

    void getRasterTiles(Integer z, Integer x, Integer y, String userId, String rasterSId, HttpServletResponse response);
}
