package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.DscMap;
import nnu.wyz.systemMS.model.entity.DscScene;
import nnu.wyz.systemMS.model.entity.PageInfo;

import java.util.List;

public interface DscMapService {
    CommonResult<PageInfo<DscMap>> getMapList(PageableDTO pageableDTO);
    CommonResult<String> deleteMap(String mapId, String userId);
    CommonResult<String> getMapUrl(String mapId);
    CommonResult<List<DscMap>> getAllMap();
}

