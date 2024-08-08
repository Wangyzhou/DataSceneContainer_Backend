package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscPublicService;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/8/6 22:32
 */
public interface DscPublicServiceDAO extends MongoRepository<DscPublicService, String> {

    DscPublicService findDscPublicServiceByServiceNameAndServiceType(String serviceName, String serviceType);

    @Query("{ 'serviceType': { $in: ['vector', 'geojson'] } }")
    List<DscPublicService> findAllVecServices();

    @Query("{ 'serviceType': { $nin: ['vector', 'geojson'] } }")
    List<DscPublicService> findAllRasServices();
}
