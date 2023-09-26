package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscFileInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DscFileDAO extends MongoRepository<DscFileInfo, String> {
}
