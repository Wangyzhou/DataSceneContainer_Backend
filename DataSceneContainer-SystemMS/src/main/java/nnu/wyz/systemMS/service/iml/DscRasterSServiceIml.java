package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscRasterSDAO;
import nnu.wyz.systemMS.dao.DscUserRasterSDAO;
import nnu.wyz.systemMS.model.dto.PublishImageDTO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.DscUserRasterS;
import nnu.wyz.systemMS.service.DscRasterSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/11/2 10:46
 */
@Service
public class DscRasterSServiceIml implements DscRasterSService {

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscRasterSDAO dscRasterSDAO;

    @Autowired
    private DscUserRasterSDAO dscUserRasterSDAO;

    @Autowired
    private MinioConfig minioConfig;

    @Override
    public CommonResult<String> publishImage2RasterS(PublishImageDTO publishImageDTO) {
        String userId = publishImageDTO.getUserId();
        String fileId = publishImageDTO.getFileId();
        String rasterSName = publishImageDTO.getName();
        List<Double> bbox = publishImageDTO.getBbox();
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到该文件!");
        }
        DscUserRasterS isExist = dscUserRasterSDAO.findDscUserRasterSByUserIdAndRasterSNameAndRasterSType(userId, rasterSName, "image");
        if (!Objects.isNull(isExist)) {
            return CommonResult.failed("存在名称相同的Image服务，请更改发布服务的名称！");
        }
        DscFileInfo dscFileInfo = byId.get();
        String rasterSUrl = minioConfig.getEndpoint() + "/" + dscFileInfo.getBucketName() + "/" + dscFileInfo.getObjectKey();
        DscRasterService dscRasterService = new DscRasterService();
        String rasterId = IdUtil.randomUUID();
        dscRasterService.setId(rasterId)
                .setPublisher(userId)
                .setPublishTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                .setName(rasterSName)
                .setFileId(fileId)
                .setBbox(bbox)
                .setType("image")
                .setOwnerCount(1L)
                .setUrl(rasterSUrl);
        dscRasterSDAO.insert(dscRasterService);
        DscUserRasterS dscUserRasterS = new DscUserRasterS();
        dscUserRasterS
                .setId(IdUtil.randomUUID())
                .setRasterSName(rasterSName)
                .setRasterSId(rasterId)
                .setUserId(userId)
                .setRasterSType("image");
        dscUserRasterSDAO.insert(dscUserRasterS);
        //增加文件发布记录
        dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() + 1);
        dscFileDAO.save(dscFileInfo);
        return CommonResult.success("发布成功!");
    }

    @Override
    public CommonResult<List<DscRasterService>> getRasterServiceList(String userId) {
        List<DscUserRasterS> dscUserRasterSList = dscUserRasterSDAO.findAllByUserId(userId);
        List<DscRasterService> dscRasterServices = dscUserRasterSList.stream().map(DscUserRasterS::getRasterSId).map(rasterSId -> dscRasterSDAO.findDscRasterServiceById(rasterSId)).collect(Collectors.toList());
        return CommonResult.success(dscRasterServices, "获取成功！");
    }

    @Override
    public CommonResult<String> deleteRasterService(String userId, String rasterSId) {
        DscUserRasterS dscUserRasterS = dscUserRasterSDAO.findByUserIdAndRasterSId(userId, rasterSId);
        if(Objects.isNull(dscUserRasterS)){
            return CommonResult.failed("未找到该服务");
        }
        DscRasterService dscRasterService = dscRasterSDAO.findDscRasterServiceById(rasterSId);
        String fileId = dscRasterService.getFileId();
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(fileId);
        if (!byId1.isPresent()) {
            return CommonResult.failed("未找到该文件");
        }
        DscFileInfo dscFileInfo = byId1.get();
        dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() - 1);
        dscFileDAO.save(dscFileInfo);
        dscUserRasterSDAO.delete(dscUserRasterS);
        dscRasterSDAO.deleteById(rasterSId);
        return CommonResult.success("删除成功!");
    }
}
