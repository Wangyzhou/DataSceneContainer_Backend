package nnu.wyz.systemMS.dao.DscCode;

import nnu.wyz.systemMS.model.entity.codeModel.GeoActTask;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GeoActTaskDAO extends MongoRepository<GeoActTask, String> {
}
