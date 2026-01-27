package nnu.wyz.systemMS.dao.DscCode;
import nnu.wyz.systemMS.model.entity.codeModel.GeoActModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GeoActModelDAO extends MongoRepository<GeoActModel, String> {
}
