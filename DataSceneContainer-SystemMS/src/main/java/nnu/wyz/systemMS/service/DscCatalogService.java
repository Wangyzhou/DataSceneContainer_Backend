package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;

import java.util.List;

public interface DscCatalogService {
    CommonResult<String> create(CreateCatalogDTO createCatalogDTO);

    void createRootCatalog(String userId);

    CommonResult<List<CatalogChildrenDTO>> getChildren(String catalogId, String userId);

    CommonResult<String> delete(String catalogId);

    /**
     * 获取只包含目录的目录树
     * @param rootCatalog
     * @return
     */
    CommonResult<List<JSONObject>> getOnlyCatalogTree(String rootCatalog);
}
