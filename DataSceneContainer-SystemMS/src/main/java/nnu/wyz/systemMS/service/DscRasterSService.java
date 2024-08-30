package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.PageInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface DscRasterSService {

    CommonResult<String> publishImage2RasterS(PublishImageDTO publishImageDTO);

    /**
     * @param publishTiff2ImageDTO
     * @param isPublic             区分公共和个人
     * @return
     */
    CommonResult<String> publishTiff2RasterS(PublishTiff2ImageDTO publishTiff2ImageDTO, boolean isPublic);

    CommonResult<String> publishTiff2TMS(PublishTiff2TMSDTO publishTiff2TMSDTO);

    /**
     *
     * @param pageableDTO
     * @param isPublic 区分公共和个人
     * @return
     */
    CommonResult<PageInfo<DscRasterService>> getRasterServiceList(PageableDTO pageableDTO, boolean isPublic);

    CommonResult<String> deleteRasterService(String userId, String rasterSId);

    CommonResult<List<DscRasterService>> getRasterServiceListByFileId(String fileId);

    void getRasterTiles(Integer z, Integer x, Integer y, String userId, String rasterSId, HttpServletResponse response);

    CommonResult<String> addRasterSCopy(GetRasterSCopyDTO getRasterSCopyDTO);

    CommonResult<String> deleteRasterSCopy(String sceneId, String rasterSId);

    /**
     * 批量更新栅格服务的ownerCount,+1或-1
     *
     * @param rasterSIds
     * @param isPlus
     */
    void updateOwnerCount(List<String> rasterSIds, boolean isPlus);

    CommonResult<String> importRasterS(ServiceShareImportDTO serviceShareImportDTO);
}
