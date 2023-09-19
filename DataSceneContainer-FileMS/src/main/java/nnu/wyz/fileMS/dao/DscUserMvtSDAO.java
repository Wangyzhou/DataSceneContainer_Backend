package nnu.wyz.fileMS.dao;

import nnu.wyz.fileMS.model.entity.DscUserMvtS;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DscUserMvtSDAO extends MongoRepository<DscUserMvtS, String> {
    List<DscUserMvtS> findAllByUserId(String userId);

    DscUserMvtS findByUserIdAndMvtId(String userId, String mvtId);

    /**
     * 检查同一用户的服务列表是否有重名
     * @param userId
     * @param mvtName
     * @return
     */
    DscUserMvtS findByUserIdAndMvtSName(String userId, String mvtName);
}
