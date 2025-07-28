package nnu.wyz.systemMS.dao.DscCode;

import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscCodeModelDAO extends MongoRepository<DscCodeModel, String> {
}
