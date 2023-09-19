package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.DeleteFileDTO;
import nnu.wyz.systemMS.model.dto.UploadFileDTO;

import javax.servlet.http.HttpServletResponse;

public interface DscFileService {

    CommonResult<String> create(UploadFileDTO uploadFileDTO);

    CommonResult<String> delete(DeleteFileDTO deleteFileDTO);

    void download(String fileId, HttpServletResponse response);

    CommonResult<JSONObject> getFileInfo(String userId, String fileId);

    CommonResult<String> getFilePreviewUrl(String fileId);
}
