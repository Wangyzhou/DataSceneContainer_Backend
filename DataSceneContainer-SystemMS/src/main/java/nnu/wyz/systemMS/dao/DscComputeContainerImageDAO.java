package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscComputeContainerImage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DscComputeContainerImageDAO extends MongoRepository<DscComputeContainerImage, String> {

    Optional<DscComputeContainerImage> findByIdentifier(String identifier);
}
