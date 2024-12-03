package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.DscUserKnowledgeInfoDAO;
import nnu.wyz.systemMS.model.dto.UserKnowledgeInfoDTO;
import nnu.wyz.systemMS.model.entity.DscUserKnowledgeInfo;
import nnu.wyz.systemMS.service.DscKnowledgeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/20 16:21
 */

@Service
@Slf4j
public class DscKnowledgeInfoServiceIml implements DscKnowledgeInfoService {

    @Autowired
    private DscUserKnowledgeInfoDAO dscUserKnowledgeInfoDAO;

    @Override
    public CommonResult<UserKnowledgeInfoDTO> getKnowledgeInfo(String userId) {
        DscUserKnowledgeInfo byUserId = dscUserKnowledgeInfoDAO.findByUserId(userId);
        UserKnowledgeInfoDTO result = byUserId == null ? new UserKnowledgeInfoDTO():
                new UserKnowledgeInfoDTO()
                        .setDatasetId(byUserId.getDatasetId()).
                        setDocumentId(byUserId.getDocumentId());
        return CommonResult.success(result, "获取成功");
    }

    @Override
    public CommonResult<UserKnowledgeInfoDTO> updateKnowledgeInfo(String userId, UserKnowledgeInfoDTO userKnowledgeInfoDTO) {
        DscUserKnowledgeInfo byUserId = dscUserKnowledgeInfoDAO.findByUserId(userId);
        if(byUserId == null) {
            dscUserKnowledgeInfoDAO.insert(new DscUserKnowledgeInfo()
                    .setId(IdUtil.randomUUID())
                    .setUserId(userId)
                    .setDatasetId(userKnowledgeInfoDTO.getDatasetId())
                    .setDocumentId(userKnowledgeInfoDTO.getDocumentId()));
        }else{
            byUserId.setDatasetId(userKnowledgeInfoDTO.getDatasetId())
                    .setDocumentId(userKnowledgeInfoDTO.getDocumentId());
            dscUserKnowledgeInfoDAO.save(byUserId);
        }
        return CommonResult.success(userKnowledgeInfoDTO, "更新成功");
    }
}
