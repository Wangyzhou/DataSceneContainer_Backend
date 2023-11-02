package nnu.wyz.systemMS.service.iml;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscVectorSDAO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import nnu.wyz.systemMS.service.DscGeoJSONService;
import nnu.wyz.systemMS.utils.GeoJSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/18 14:16
 */
@SuppressWarnings("rawtypes")
@Service
@Slf4j
public class DscGeoJSONServiceIml implements DscGeoJSONService {

    @Autowired
    private DscVectorSDAO dscVectorSDAO;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Value("${fileSavePath}")
    private String fileRootPath;

    @Value("${fileSavePathWin}")
    private String fileRootPathWin;

    @Override
    public CommonResult<List<String>> getFields(String vectorSId) {
        CommonResult initialGeoJSONUtilResult = this.initialGeoJSONUtil(vectorSId);
        if(initialGeoJSONUtilResult.getCode() != 200) {
            return initialGeoJSONUtilResult;
        }
        List<String> fields = GeoJSONUtil.getFields();
        return CommonResult.success(fields, "获取成功！");
    }

    @Override
    public CommonResult<List> getUniqueValues(String vectorSId, String field, String method) {
        CommonResult initialGeoJSONUtilResult = this.initialGeoJSONUtil(vectorSId);
        if(initialGeoJSONUtilResult.getCode() != 200) {
            return initialGeoJSONUtilResult;
        }
        List uniqueValues = GeoJSONUtil.getUniqueValues(field, method);
        return CommonResult.success(uniqueValues, "获取成功!");
    }

    @Override
    public CommonResult<Integer> getFeatureCount(String vectorSId) {
        CommonResult initialGeoJSONUtilResult = this.initialGeoJSONUtil(vectorSId);
        if(initialGeoJSONUtilResult.getCode() != 200) {
            return initialGeoJSONUtilResult;
        }
        return CommonResult.success(GeoJSONUtil.getFeatureCount(), "获取成功!");
    }

    @Override
    public CommonResult<List<Map<String, Object>>> getAttrs(String vectorSId) {
        CommonResult initialGeoJSONUtilResult = this.initialGeoJSONUtil(vectorSId);
        if(initialGeoJSONUtilResult.getCode() != 200) {
            return initialGeoJSONUtilResult;
        }
        List<Map<String, Object>> attrs = GeoJSONUtil.getAttrs();
        return CommonResult.success(attrs, "获取成功!");
    }

    private CommonResult initialGeoJSONUtil(String vectorSId) {
        Optional<DscVectorServiceInfo> byId = dscVectorSDAO.findById(vectorSId);
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该服务！");
        }
        DscVectorServiceInfo dscVectorServiceInfo = byId.get();
        String fileId = dscVectorServiceInfo.getFileId();
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(fileId);
        if(!byId1.isPresent()) {
            return CommonResult.failed("文件未找到！");
        }
        DscFileInfo dscFileInfo = byId1.get();
        String bucketName = dscFileInfo.getBucketName();
        String objectKey = dscFileInfo.getObjectKey();
        String fileRoot = System.getProperty("os.name").startsWith("Windows") ? fileRootPathWin : fileRootPath;
        String fullPath = fileRoot + bucketName + File.separator + objectKey;
        CommonResult initUtilResult = GeoJSONUtil.initUtil(fullPath);
        if(initUtilResult.getCode() != 200) {
            return initUtilResult;
        }
        return CommonResult.success("初始化工具成功！");
    }
}
