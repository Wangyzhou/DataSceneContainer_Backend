package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PublishGeoJSONDTO;
import nnu.wyz.systemMS.model.dto.PublishShapefileDTO;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DscVectorSService {

    CommonResult<String> publishShp2VectorS(PublishShapefileDTO publishShapefileDTO);

    void getMvt(int zoom, int x, int y, String tableName, HttpServletResponse response);

    CommonResult<List<DscVectorServiceInfo>> getVectorServiceList(String userId);

    CommonResult<String> deleteVectorService(String userId, String vectorSId);

    CommonResult<List<DscVectorServiceInfo>> getVectorServicesByFileId(String fileId);

    CommonResult<String> publishGeoJSON2VectorS(PublishGeoJSONDTO publishGeoJSONDTO);
}
