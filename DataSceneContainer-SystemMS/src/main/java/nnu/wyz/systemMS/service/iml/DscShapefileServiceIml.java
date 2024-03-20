package nnu.wyz.systemMS.service.iml;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.ShpProcessDAO;
import nnu.wyz.systemMS.service.DscShapeFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/8 10:32
 */
@Service
public class DscShapefileServiceIml implements DscShapeFileService {

    @Autowired
    private ShpProcessDAO shpProcessDAO;

    @Override
    public CommonResult<List<String>> getFields(String tableName) {
        List<String> fields = shpProcessDAO.getFields(tableName);
        return CommonResult.success(fields, "获取成功！");
    }

    @Override
    public CommonResult<List<String>> getNumericFields(String tableName) {
        List<String> fields = shpProcessDAO.getNumericFields(tableName);
        return CommonResult.success(fields, "获取成功！");
    }

    @Override
    public CommonResult<List<Object>> getUniqueValues(String ptName, String field, String method) {
        List<Object> uniqueValues = shpProcessDAO.getUniqueValues(ptName, field, method);
        return CommonResult.success(uniqueValues, "获取成功！");
    }

    @Override
    public CommonResult<Long> getFeatureCount(String tableName) {
        Long featureCount = shpProcessDAO.getFeatureCount(tableName);
        return CommonResult.success(featureCount, "获取成功！");
    }

    @Override
    public CommonResult<List<Map<String, Object>>> getShpAttrInfoFromPG(String tableName) {
        List<Map<String, Object>> shpAttrInfoFromPG = shpProcessDAO.getShpAttrInfoFromPG(tableName);
        return CommonResult.success(shpAttrInfoFromPG, "获取成功！");
    }

    @Override
    public CommonResult<List<Map<String, Object>>> getCenterAndAttrByFields(String tableName, String[] fields) {
        List<Map<String, Object>> centerAndAttrInfo = shpProcessDAO.getCenterAndAttrByFields(tableName, fields);
        return CommonResult.success(centerAndAttrInfo, "获取成功！");
    }
}
