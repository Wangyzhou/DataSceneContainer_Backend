package nnu.wyz.systemMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.DscPublicFile;
import nnu.wyz.systemMS.model.entity.DscPublicService;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import nnu.wyz.systemMS.model.entity.PageInfo;

public interface DscPublicResourceService {

    CommonResult<String> publishResource();

    CommonResult<String> deleteFile(String fileId);

    CommonResult<PageInfo<DscPublicFile>> getFileList(PageableDTO pageableDTO);

    CommonResult<String> deleteService(String serviceId);
}
