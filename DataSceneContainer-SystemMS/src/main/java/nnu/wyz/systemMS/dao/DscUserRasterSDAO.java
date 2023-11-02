package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscUserRasterS;
import nnu.wyz.systemMS.model.entity.DscUserVectorS;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DscUserRasterSDAO extends MongoRepository<DscUserRasterS, String> {
    List<DscUserRasterS> findAllByUserId(String userId);

    DscUserRasterS findByUserIdAndRasterSId(String userId, String rasterId);


    DscUserRasterS findDscUserRasterSByUserIdAndRasterSNameAndRasterSType(String userId, String rasterSName, String rasterSType);
    /**
     * 检查同一用户的服务列表是否有重名
     * @param userId
     * @param rasterName
     * @return DscUserRasterS
     */
    DscUserRasterS findByUserIdAndRasterSName(String userId, String rasterName);
}
