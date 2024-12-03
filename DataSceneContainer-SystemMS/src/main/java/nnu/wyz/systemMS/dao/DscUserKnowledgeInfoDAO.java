package nnu.wyz.systemMS.dao;

import nnu.wyz.systemMS.model.dto.UserKnowledgeInfoDTO;
import nnu.wyz.systemMS.model.entity.DscUserKnowledgeInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/20 16:07
 */
public interface DscUserKnowledgeInfoDAO extends MongoRepository<DscUserKnowledgeInfo, String> {

    DscUserKnowledgeInfo findByUserId(String userId);
}
