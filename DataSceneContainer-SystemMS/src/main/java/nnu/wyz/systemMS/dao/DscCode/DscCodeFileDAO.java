package nnu.wyz.systemMS.dao.DscCode;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DscCodeFileDAO extends MongoRepository<DscCodeFile, String> {
    // 在这里添加一些自定义查询方法
    List<DscCodeFile> findBycreatedUserID(String userID);
}
