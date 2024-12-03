package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.UserKnowledgeInfoDTO;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/20 16:17
 */
public interface DscKnowledgeInfoService {

    CommonResult<UserKnowledgeInfoDTO> getKnowledgeInfo(String userId);

    CommonResult<UserKnowledgeInfoDTO> updateKnowledgeInfo(String userId, UserKnowledgeInfoDTO userKnowledgeInfoDTO);
}
