package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.FileTagDTO;
import nnu.wyz.systemMS.model.dto.UpdateFileTagDTO;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/18 20:13
 */
public interface DscFileTagService {

    CommonResult<FileTagDTO> getFileTag(String fileId);

    CommonResult<String> updateFileTag(String userId, String fileId, UpdateFileTagDTO updateFileTagDTO);

    CommonResult<String> deleteFileTag(String fileId);

    CommonResult<List<FileTagDTO>> getAllFileTag(String userId);
}
