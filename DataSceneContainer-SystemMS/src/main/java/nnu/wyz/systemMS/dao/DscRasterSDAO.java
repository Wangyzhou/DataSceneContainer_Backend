package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscRasterService;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DscRasterSDAO extends MongoRepository<DscRasterService, String> {
    DscRasterService findDscRasterServiceById(String rasterSId);

    List<DscRasterService> findAllByFileId(String fileId);
}
