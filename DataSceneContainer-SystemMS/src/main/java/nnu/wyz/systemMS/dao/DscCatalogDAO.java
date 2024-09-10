package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscCatalog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscCatalogDAO extends MongoRepository<DscCatalog, String> {

    /**
     * 同一级的目录名不能重复，保险起见，加上userId作为查询条件
     *
     * @param catalogName
     * @param parent
     * @return
     */
    DscCatalog findDscCatalogByNameAndUserIdAndParent(String catalogName, String userId, String parent);

    /**
     * 根据目录Id和用户Id获取目录
     *
     * @param catalogId
     * @return
     */
    DscCatalog findDscCatalogById(String catalogId);

    /**
     * 根据user和父目录ID获取目录
     * 用于获取每个用户的场景数据根目录
     * @param userId
     * @param parentId
     * @return
     */
    DscCatalog findDscCatalogByUserIdAndParent(String userId, String parentId);

    /**
     * 根据user和父目录ID和TaskId获取目录
     *
     * @param parentId
     * @param taskId
     * @return
     */
    DscCatalog findDscCatalogByUserIdAndParentAndTaskId(String userId, String parentId, String taskId);

}
