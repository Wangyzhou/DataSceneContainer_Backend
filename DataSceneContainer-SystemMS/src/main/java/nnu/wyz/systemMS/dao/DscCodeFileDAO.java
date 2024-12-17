package nnu.wyz.systemMS.dao;
import nnu.wyz.systemMS.model.entity.DscCodeFile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DscCodeFileDAO extends MongoRepository<DscCodeFile, String> {
    // 在这里添加一些自定义查询方法
}
