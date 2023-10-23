package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscUserVectorS;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DscUserVectorSDAO extends MongoRepository<DscUserVectorS, String> {
    List<DscUserVectorS> findAllByUserId(String userId);

    DscUserVectorS findByUserIdAndVectorSId(String userId, String mvtId);


    DscUserVectorS findDscUserVectorSByUserIdAndVectorSNameAndVectorSType(String userId, String vectorSName, String vectorSType);
    /**
     * 检查同一用户的服务列表是否有重名
     * @param userId
     * @param mvtName
     * @return
     */
    DscUserVectorS findByUserIdAndVectorSName(String userId, String mvtName);
}
