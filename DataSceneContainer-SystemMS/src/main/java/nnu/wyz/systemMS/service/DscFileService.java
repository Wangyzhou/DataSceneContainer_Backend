package nnu.wyz.systemMS.service;

import com.alibaba.fastjson.JSONObject;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.*;

import javax.servlet.http.HttpServletResponse;

public interface DscFileService {

    CommonResult<String> create(UploadFileDTO uploadFileDTO);

    CommonResult<String> delete(DeleteFileDTO deleteFileDTO);

    void download(String fileId, HttpServletResponse response);

    CommonResult<JSONObject> getFileInfo(String fileId);

    CommonResult<String> getFilePreviewUrl(String fileId);

    CommonResult<String> shareFile(FileShareDTO fileShareDTO);

    CommonResult<String> importResource(FileShareImportDTO fileShareImportDTO);

    CommonResult<String> unzip(UnzipFileDTO unzipFileDTO);

}
