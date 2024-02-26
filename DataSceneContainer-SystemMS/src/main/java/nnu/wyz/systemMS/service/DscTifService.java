package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.RenderTifDTO;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/2/22 15:42
 */
public interface DscTifService {

    /**
     * 获取波段数量
     * @param userId
     * @param rasterSId
     * @return
     */
    CommonResult<Integer> getBandCount(String userId, String rasterSId);

    /**
     * @param renderTifDTO
     * @return
     */
    CommonResult<String> changeColorMap(RenderTifDTO renderTifDTO);
}
