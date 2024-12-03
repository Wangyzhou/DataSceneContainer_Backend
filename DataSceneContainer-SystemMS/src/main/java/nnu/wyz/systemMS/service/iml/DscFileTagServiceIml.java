package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscFileTagDAO;
import nnu.wyz.systemMS.model.dto.FileTagDTO;
import nnu.wyz.systemMS.model.dto.UpdateFileTagDTO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.DscFileTag;
import nnu.wyz.systemMS.service.DscFileTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/18 20:16
 */

@Service
@Slf4j
public class DscFileTagServiceIml implements DscFileTagService {

    @Autowired
    private DscFileTagDAO dscFileTagDAO;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Override
    public CommonResult<FileTagDTO> getFileTag(String fileId) {
        Optional<DscFileInfo> file = dscFileDAO.findById(fileId);
        if (!file.isPresent()) {
            return CommonResult.failed("文件不存在");
        }
        DscFileTag byId = dscFileTagDAO.findByFileId(fileId);
        DscFileTag dscFileTag = byId != null ? byId : new DscFileTag();
        String fileName = dscFileDAO.findById(fileId).get().getFileName();
        FileTagDTO result = new FileTagDTO();
        result.setFileId(fileId)
                .setFileName(fileName)
                .setType(dscFileTag.getType())
                .setLocation(dscFileTag.getLocation())
                .setAttr(dscFileTag.getAttr())
                .setOther(dscFileTag.getOther());
        return CommonResult.success(result);
    }

    @Override
    public CommonResult<String> updateFileTag(String userId, String fileId, UpdateFileTagDTO updateFileTagDTO) {
        Optional<DscFileInfo> file = dscFileDAO.findById(fileId);
        if (!file.isPresent()) {
            return CommonResult.failed("文件不存在");
        }
        DscFileTag byId = dscFileTagDAO.findByFileId(fileId);
        if (byId == null) {
            DscFileTag dscFileTag = new DscFileTag();
            dscFileTag.setId(IdUtil.randomUUID())
                    .setCreatedUser(userId)
                    .setFileId(fileId)
                    .setType(updateFileTagDTO.getType())
                    .setLocation(updateFileTagDTO.getLocation())
                    .setAttr(updateFileTagDTO.getAttr())
                    .setOther(updateFileTagDTO.getOther());
            dscFileTagDAO.insert(dscFileTag);
            return CommonResult.success("add", "创建成功");
        } else {
            byId.setType(updateFileTagDTO.getType()).setLocation(updateFileTagDTO.getLocation()).setAttr(updateFileTagDTO.getAttr()).setOther(updateFileTagDTO.getOther());
            dscFileTagDAO.save(byId);
            return CommonResult.success("update", "更新成功");
        }
    }

    @Override
    public CommonResult<String> deleteFileTag(String fileId) {
        dscFileTagDAO.deleteByFileId(fileId);
        return CommonResult.success("删除成功");
    }

    @Override
    public CommonResult<List<FileTagDTO>> getAllFileTag(String userId) {
        List<FileTagDTO> allByCreatedUser = dscFileTagDAO.findAllByCreatedUser(userId);
        allByCreatedUser.stream().map(fileTagDTO -> {
            // 根据 fileId 获取对应的文件名称
            Optional<DscFileInfo> byId = dscFileDAO.findById(fileTagDTO.getFileId());
            if(!byId.isPresent()) {
                fileTagDTO.setFileName("未知文件");
            }else{
                fileTagDTO.setFileName(byId.get().getFileName());
            }
            return fileTagDTO;
        }).collect(Collectors.toList());
        return CommonResult.success(allByCreatedUser, "获取成功");
    }
}
