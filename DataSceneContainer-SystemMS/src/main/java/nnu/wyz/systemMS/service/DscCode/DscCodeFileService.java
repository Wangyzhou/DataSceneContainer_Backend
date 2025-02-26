package nnu.wyz.systemMS.service.DscCode;

import nnu.wyz.systemMS.model.dto.DscCodeFileDTO;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeFile;

import java.util.List;

public interface DscCodeFileService {
    void saveCodeFile(DscCodeFileDTO codeFileDTO);
    List<DscCodeFile> getFileList(String userId);
    boolean deleteCodeFile(String id);
    boolean renameFileName(String id, String newFileName);
}
