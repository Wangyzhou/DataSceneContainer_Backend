package nnu.wyz.dao;

import nnu.wyz.entity.DscCatalog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscCatalogDAO extends MongoRepository<DscCatalog, String> {
    /**
     * 根据user和父目录ID获取目录
     * @param userId
     * @param parentId
     * @return
     */
    DscCatalog findDscCatalogByUserIdAndParent(String userId, String parentId);
}
