package nnu.wyz.fileMS.service;

import nnu.wyz.domain.CommonResult;

import java.util.List;
import java.util.Map;

public interface DscShapefileService {

    /**
     * 获取字段名
     * @param tableName
     * @return
     */
    CommonResult<List<String>> getFields(String tableName);

    /**
     * 获取某字段的唯一值升/降序数组
     * @param ptName
     * @param field
     * @param method
     * @return
     */
    CommonResult<List<Object>> getUniqueValues(String ptName, String field, String method);

    /**
     * 获取要素个数
     * @param tableName
     * @return
     */
    CommonResult<Long> getFeatureCount(String tableName);

    /**
     * 获取属性表
     * @param tableName
     * @return
     */
    CommonResult<List<Map<String, Object>>> getShpAttrInfoFromPG(String tableName);
}
