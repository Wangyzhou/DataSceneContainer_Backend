package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscMap;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscMapDao extends MongoRepository<DscMap, String> {
    DscMap findDscMapById(String id, String userId);
    DscMap findDscMapByName(String name, String userId);
}
