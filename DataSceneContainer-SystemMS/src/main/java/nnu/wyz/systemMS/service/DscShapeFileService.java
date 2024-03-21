package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/13 20:34
 */

public interface DscShapeFileService {

    /**
     * 获取字段名
     * @param tableName
     * @return
     */
    CommonResult<List<String>> getFields(String tableName);

    /**
     * 获取数值型字段名
     * @param tableName
     * @return
     */
    CommonResult<List<String>> getNumericFields(String tableName);

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

    /**
     * 获取要素中心及指定字段值
     * @param tableName
     * @param fields
     * @return
     */
    CommonResult<List<Map<String, Object>>> getCenterAndAttrByFields(String tableName, String[] fields);
}
