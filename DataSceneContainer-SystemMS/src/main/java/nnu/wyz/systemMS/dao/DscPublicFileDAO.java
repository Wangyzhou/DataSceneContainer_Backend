package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.entity.DscPublicFile;
import org.springframework.data.mongodb.repository.MongoRepository;


/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/8/5 20:47
 */
public interface DscPublicFileDAO extends MongoRepository<DscPublicFile, String> {

}
