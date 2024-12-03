package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.dto.FileTagDTO;
import nnu.wyz.systemMS.model.entity.DscFileTag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/18 20:19
 */
public interface DscFileTagDAO extends MongoRepository<DscFileTag, String> {

    DscFileTag findByFileId(String fileId);

    void deleteByFileId(String fileId);

    List<FileTagDTO> findAllByCreatedUser(String userId);
}
