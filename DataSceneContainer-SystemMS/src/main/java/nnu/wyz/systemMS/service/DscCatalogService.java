package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.PageInfo;

import java.util.List;

public interface DscCatalogService {
    CommonResult<String> create(CreateCatalogDTO createCatalogDTO);

    void createRootCatalog(String userId);

    /**
     * 创建隐藏的场景数据根目录
     * @param userId
     */
    void createSceneDataRootCatalog(String userId);

    CommonResult<List<CatalogChildrenDTO>> getChildren(String catalogId);

    CommonResult<String> delete(String catalogId);

    /**
     * 获取只包含目录的目录树
     * @param rootCatalog
     * @return
     */
    CommonResult<List<JSONObject>> getOnlyCatalogTree(String rootCatalog);

    /**
     * 获取完整的目录树（既包含目录，也包含文件）
     * @param rootCatalog
     * @return
     */
    CommonResult<List<JSONObject>> getCatalogChildrenTree(String rootCatalog);

    CommonResult<PageInfo<CatalogChildrenDTO>> getChildrenByPageable(PageableDTO pageableDTO);

    CommonResult<String> pwd(String catalogId);

    /**
     * 获取目录物理路径
     * @param catalogId
     * @return
     */
    String getPhysicalPath(String catalogId);

}
