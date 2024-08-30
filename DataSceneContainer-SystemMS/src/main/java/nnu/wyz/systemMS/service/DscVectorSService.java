package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.PublishGeoJSONDTO;
import nnu.wyz.systemMS.model.dto.PublishShapefileDTO;
import nnu.wyz.systemMS.model.dto.ServiceShareImportDTO;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import nnu.wyz.systemMS.model.entity.PageInfo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DscVectorSService {

    CommonResult<String> publishShp2VectorS(PublishShapefileDTO publishShapefileDTO);

    void getMvt(int zoom, int x, int y, String tableName, HttpServletResponse response);

    /**
     *
     * @param pageableDTO
     * @param isPublic 区分公共和个人
     * @return
     */
    CommonResult<PageInfo<DscVectorServiceInfo>> getVectorServiceList(PageableDTO pageableDTO, boolean isPublic);

    CommonResult<String> deleteVectorService(String userId, String vectorSId);

    CommonResult<List<DscVectorServiceInfo>> getVectorServicesByFileId(String fileId);

    /**
     *
     * @param publishGeoJSONDTO
     * @param isPublic 区分公共和个人
     * @return
     */
    CommonResult<String> publishGeoJSON2VectorS(PublishGeoJSONDTO publishGeoJSONDTO, boolean isPublic);

    CommonResult<List<DscVectorServiceInfo>> getVectorServiceListByFileId(String fileId);

    /**
     * 批量更新矢量服务的ownerCount,+1或-1
     *
     * @param vectorSIds
     * @param isPlus
     */
    void updateOwnerCount(List<String> vectorSIds, boolean isPlus);

    CommonResult<String> importVectorS(ServiceShareImportDTO serviceShareImportDTO);
}
