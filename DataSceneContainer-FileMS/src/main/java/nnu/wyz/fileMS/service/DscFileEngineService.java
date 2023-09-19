package nnu.wyz.fileMS.service;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.fileMS.model.dto.PublishShapefileDTO;
import nnu.wyz.fileMS.model.dto.UnzipFileDTO;

public interface DscFileEngineService {

    CommonResult<String> unzip(UnzipFileDTO unzipFileDTO);

    CommonResult<String> publishShapefile(PublishShapefileDTO publishShapefileDTO);

}
