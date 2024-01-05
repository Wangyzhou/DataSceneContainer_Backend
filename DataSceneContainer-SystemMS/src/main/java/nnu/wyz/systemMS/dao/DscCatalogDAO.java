package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscCatalog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscCatalogDAO extends MongoRepository<DscCatalog, String> {

    /**
     * 同一级的目录名不能重复，保险起见，加上userId作为查询条件
     * @param catalogName
     * @param level
     * @return
     */
    DscCatalog findDscCatalogByNameAndUserIdAndLevel(String catalogName, String userId, Integer level);

    /**
     * 根据目录Id和用户Id获取目录
     * @param catalogId
     * @return
     */
    DscCatalog findDscCatalogById(String catalogId);

    /**
     * 根据user和父目录ID获取目录
     * @param userId
     * @param parentId
     * @return
     */
    DscCatalog findDscCatalogByUserIdAndParent(String userId, String parentId);

}
