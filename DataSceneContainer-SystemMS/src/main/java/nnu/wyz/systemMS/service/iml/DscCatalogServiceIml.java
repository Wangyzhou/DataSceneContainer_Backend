package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.CreateCatalogDTO;
import nnu.wyz.systemMS.model.dto.DeleteFileDTO;
import nnu.wyz.systemMS.model.entity.DscCatalog;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 10:55
 */
@Service
public class DscCatalogServiceIml implements DscCatalogService {

    @Autowired
    private DscCatalogDAO dscCatalogDAO;

    @Autowired
    private DscFileService dscFileService;

    @Override
    public CommonResult<String> create(CreateCatalogDTO createCatalogDTO) {
        String parentCatalogId = createCatalogDTO.getParentCatalogId();
        Optional<DscCatalog> parentCatalogOptional = dscCatalogDAO.findById(parentCatalogId);
        if (!parentCatalogOptional.isPresent()) {
            return CommonResult.failed(ResultCode.FAILED, "未找到载体目录！");
        }
        DscCatalog parentCatalog = parentCatalogOptional.get();
        Integer preLevel = parentCatalog.getLevel();
        String catalogName = createCatalogDTO.getCatalogName();
        String userId = createCatalogDTO.getUserId();
        DscCatalog conflictCatalog = dscCatalogDAO.findDscCatalogByNameAndUserIdAndLevel(catalogName, userId, preLevel + 1);
        if (!Objects.isNull(conflictCatalog)) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "创建目录失败！存在同名目录，请确保同级目录名唯一！");
        }
        //1、创建目录
        DscCatalog dscCatalog = new DscCatalog();
        String catalogId = UUID.randomUUID().toString();
        String dateTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        dscCatalog.setId(catalogId)
                .setName(catalogName)
                .setUserId(userId)
                .setTotal(0)
                .setLevel(preLevel + 1)
                .setParent(parentCatalogId)
                .setChildren(new ArrayList<>())
                .setCreatedTime(dateTime)
                .setUpdatedTime(dateTime);
        dscCatalogDAO.insert(dscCatalog);
        //2、父目录增加孩子节点
        CatalogChildrenDTO catalogChildrenDTO = new CatalogChildrenDTO();
        catalogChildrenDTO.setId(catalogId)
                .setName(catalogName)
                .setType("folder")
                .setUpdatedTime(dateTime);
        parentCatalog.getChildren().add(catalogChildrenDTO);
        parentCatalog.setUpdatedTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        parentCatalog.setTotal(parentCatalog.getTotal() + 1);
        dscCatalogDAO.save(parentCatalog);
        return CommonResult.success("创建目录成功！");
    }

    @Override
    public void createRootCatalog(String userId) {
        DscCatalog dscCatalog = new DscCatalog();
        String dateTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        dscCatalog.setId(UUID.randomUUID().toString())
                .setName("MyData")
                .setUserId(userId)
                .setTotal(0)
                .setLevel(0)
                .setParent("-1")
                .setChildren(new ArrayList<>())
                .setCreatedTime(dateTime)
                .setUpdatedTime(dateTime);
        dscCatalogDAO.insert(dscCatalog);
    }

    @Override
    public CommonResult<List<CatalogChildrenDTO>> getChildren(String catalogId, String userId) {
        DscCatalog dscCatalog = dscCatalogDAO.findDscCatalogByIdAndUserId(catalogId, userId);
        if (Objects.isNull(dscCatalog)) {
            return CommonResult.failed();
        }
        return CommonResult.success(dscCatalog.getChildren());
    }

    /**
     * 递归删除catalog
     *
     * @param catalogId
     * @return
     */
    @Override
    public CommonResult<String> delete(String catalogId) {
        Boolean isDelete = this.deleteByRecursion(catalogId);
        return isDelete ? CommonResult.success("删除成功！") : CommonResult.failed("删除失败！");
    }
//    @MongoTransactional
//    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class, timeout = 120)
    public Boolean deleteByRecursion(String catalogId) {
        //递归出口：当前目录为空目录
        Optional<DscCatalog> byId = dscCatalogDAO.findById(catalogId);
        DscCatalog dscCatalog = byId.get();
        if (dscCatalog.getTotal() == 0) {
             this.deleteEmptyCatalog(catalogId, dscCatalog.getParent());
            return true;
        }
        //遍历孩子节点，循环删除
        List<CatalogChildrenDTO> children = dscCatalog.getChildren();
        for (CatalogChildrenDTO catalogChildrenDTO : children) {
            if (catalogChildrenDTO.getType().equals("folder")) {
                Boolean isFolderDelete = deleteByRecursion(catalogChildrenDTO.getId());
                if(!isFolderDelete) {
                   throw new RuntimeException("删除失败，未知的错误！");
                }
            } else {
                DeleteFileDTO deleteFileDTO = new DeleteFileDTO();
                deleteFileDTO.setUserId(dscCatalog.getUserId())
                        .setCatalogId(catalogId)
                        .setFileId(catalogChildrenDTO.getId());
                dscFileService.delete(deleteFileDTO);
            }
        }
        //删除该目录
        this.deleteEmptyCatalog(catalogId, dscCatalog.getParent());
        return true;
    }
//    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class, timeout = 120)
//    @MongoTransactional
    public void deleteEmptyCatalog(String catalogId, String parentCatalogId) {
        Optional<DscCatalog> byId = dscCatalogDAO.findById(parentCatalogId);
        DscCatalog parentCatalog = byId.get();
        List<CatalogChildrenDTO> children = parentCatalog.getChildren();
        Iterator<CatalogChildrenDTO> iterator = children.iterator();
        while (iterator.hasNext()) {
            CatalogChildrenDTO temp = iterator.next();
            if (temp.getId().equals(catalogId)) {
                iterator.remove();
                break;
            }
        }
        parentCatalog.setTotal(parentCatalog.getTotal() - 1);
        parentCatalog.setUpdatedTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscCatalogDAO.save(parentCatalog);
        dscCatalogDAO.deleteById(catalogId);
    }

    public CommonResult<List<JSONObject>> getOnlyCatalogTree(String rootCatalog) {
        List<JSONObject> catalogList = this.recursion(rootCatalog);
        JSONObject rootList = new JSONObject();
        rootList.put("id", rootCatalog);
        rootList.put("label", "MyData");
        rootList.put("children", catalogList);
        ArrayList<JSONObject> root = new ArrayList<>();
        root.add(rootList);
        return CommonResult.success(root, "获取成功！");
    }

    private List<JSONObject> recursion(String catalogId) {
        Optional<DscCatalog> byId = dscCatalogDAO.findById(catalogId);
        DscCatalog dscCatalog = byId.get();
        if (dscCatalog.getChildren().size() == 0) {
            return null;
        }
        ArrayList<JSONObject> catalogItems = new ArrayList<>();
        for (CatalogChildrenDTO childrenDTO :
                dscCatalog.getChildren()) {
            if (childrenDTO.getType().equals("folder")) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", childrenDTO.getId());
                jsonObject.put("label", childrenDTO.getName());
                List<JSONObject> result = recursion(childrenDTO.getId());
                jsonObject.put("children", result);
                catalogItems.add(jsonObject);
            }
        }
        return catalogItems;
    }
}
