package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscGeoAnalysisDAO;
import nnu.wyz.systemMS.model.DscGeoAnalysis.DscGeoAnalysisTool;
import nnu.wyz.systemMS.model.dto.ConvertSgrd2GeoTIFFDTO;
import nnu.wyz.systemMS.model.dto.TaskInfoDTO;
import nnu.wyz.systemMS.model.dto.UploadFileDTO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.service.DscCatalogService;
import nnu.wyz.systemMS.service.DscFileService;
import nnu.wyz.systemMS.service.DscGeoAnalysisToolService;
import nnu.wyz.systemMS.service.SysUploadTaskService;
import nnu.wyz.systemMS.utils.SagaOtherToolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

/**
 * @description:
 * @author: yzwang
 * @time: 2024/1/11 16:49
 */
@Service
@Slf4j
public class DscGeoAnalysisToolServiceIml implements DscGeoAnalysisToolService {

    @Autowired
    private DscGeoAnalysisDAO dscGeoAnalysisDAO;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private SysUploadTaskService sysUploadTaskService;

    @Autowired
    private DscFileService dscFileService;

    @Value("${fileSavePath}")
    private String root;

    @Override
    public CommonResult<DscGeoAnalysisTool> getGeoAnalysisTool(String toolId) {
        Optional<DscGeoAnalysisTool> byId = dscGeoAnalysisDAO.findById(toolId);
        return byId.map(dscGeoAnalysisTool -> CommonResult.success(dscGeoAnalysisTool, "获取工具成功")).orElseGet(() -> CommonResult.failed("未找到该工具"));
    }

    @Override
    public CommonResult<String> convertSgrd2Geotiff(ConvertSgrd2GeoTIFFDTO convertSgrd2GeoTIFFDTO) {
        Optional<DscFileInfo> byId = dscFileDAO.findById(convertSgrd2GeoTIFFDTO.getSgrdFile());
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该文件");
        }
        DscFileInfo dscFileInfo = byId.get();
        String sgrdFilePath = root + dscFileInfo.getBucketName() + "/" + dscFileInfo.getObjectKey();
        String geoTiffId = IdUtil.randomUUID();
        String geoTiffFilePath = root + minioConfig.getBucketName() + "/" + convertSgrd2GeoTIFFDTO.getUserId() + "/" + geoTiffId + ".tif";
        boolean isConvert = SagaOtherToolUtil.ConvertSgrd2GeoTIFF(sgrdFilePath, geoTiffFilePath);
        if (!isConvert) {
            return CommonResult.failed("转换失败");
        }
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(geoTiffFilePath);
            File file = new File(geoTiffFilePath);
            String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
            String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            String fileName = dscFileInfo.getFileName().substring(0, dscFileInfo.getFileName().lastIndexOf(".")) + ".tif";
            String fileId = IdUtil.objectId();
            DscFileInfo geoTiffInfo = new DscFileInfo(fileId, md5, fileName, suffix, false, convertSgrd2GeoTIFFDTO.getUserId(), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"), file.length(), 0L, 0L, 0L, 0L, minioConfig.getBucketName(), convertSgrd2GeoTIFFDTO.getUserId() + "/" + file.getName(), 32);
            dscFileDAO.insert(geoTiffInfo);
            InitTaskParam initTaskParam = new InitTaskParam();
            initTaskParam.setIdentifier(md5);
            initTaskParam.setFileName(file.getName());
            initTaskParam.setFileId(fileId);
            initTaskParam.setUserId(convertSgrd2GeoTIFFDTO.getUserId());
            initTaskParam.setTotalSize(file.length());
            initTaskParam.setChunkSize(file.length());
            initTaskParam.setObjectName(file.getName().substring(0, file.getName().lastIndexOf(".")));
            TaskInfoDTO taskInfoDTO = sysUploadTaskService.initTask(initTaskParam);
            UploadFileDTO uploadFileDTO = new UploadFileDTO(convertSgrd2GeoTIFFDTO.getUserId(), taskInfoDTO.getTaskRecord().getId(), convertSgrd2GeoTIFFDTO.getOutputDir());
            dscFileService.create(uploadFileDTO);
            return CommonResult.success("转换成功");
        } catch (IOException e) {
            log.error(e.getMessage());
            return CommonResult.failed("转换失败");
        }
    }
}
