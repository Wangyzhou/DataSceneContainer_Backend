package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.entity.DscTable;

/**
 * @author tjk
 * @date 2024/12/19
 * @Description
 */
public interface DscTableService {
    // 根据 ID 获取文件路径并读取数据
    CommonResult<DscTable> getDscTableById(String id);


//    DscTable getTableDataById(String id);
}
