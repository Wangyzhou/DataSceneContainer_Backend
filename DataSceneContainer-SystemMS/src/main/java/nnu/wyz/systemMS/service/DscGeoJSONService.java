package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;

import java.util.List;
import java.util.Map;

public interface DscGeoJSONService {

    CommonResult<List<String>> getFields(String vectorSId);

    CommonResult<List> getUniqueValues(String vectorSId, String field, String method);

    CommonResult<Integer> getFeatureCount(String vectorSId);

    CommonResult<List<Map<String, Object>>> getAttrs(String vectorSId);
}
