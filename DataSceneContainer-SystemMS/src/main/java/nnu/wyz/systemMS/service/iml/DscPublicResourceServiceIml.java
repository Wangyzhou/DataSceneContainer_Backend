package nnu.wyz.systemMS.service.iml;

import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.DscPublicResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/8/5 21:18
 */

@Service
public class DscPublicResourceServiceIml implements DscPublicResourceService {

    @Autowired
    DscPublicFileDAO dscPublicFileDAO;

    @Autowired
    DscPublicServiceDAO dscPublicServiceDAO;

    @Autowired
    DscFileDAO dscFileDAO;

    @Autowired
    DscVectorSDAO dscVectorSDAO;

    @Autowired
    DscRasterSDAO dscRasterSDAO;

    @Override
    public CommonResult<String> publishResource() {
        return null;
    }

    @Override
    public CommonResult<String> deleteFile(String fileId) {
        Optional<DscPublicFile> byId = dscPublicFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            CommonResult.failed("文件不存在！");
        }
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(fileId);
        if (!byId1.isPresent()) {
            CommonResult.failed("文件信息不存在！");
        }
        DscPublicFile dscPublicFile = byId.get();
        DscFileInfo dscFileInfo = byId1.get();
        dscFileInfo.setOwnerCount(dscFileInfo.getOwnerCount() - 1);
        dscPublicFileDAO.delete(dscPublicFile);
        return CommonResult.success("删除成功");
    }

    @Override
    public CommonResult<PageInfo<DscPublicFile>> getFileList(PageableDTO pageableDTO) {
        List<DscPublicFile> fsListNoLimit = dscPublicFileDAO.findAll();
        List<DscPublicFile> dscPublicFileList = fsListNoLimit
                .stream()
                .filter(dscPublicFile -> dscPublicFile.getName().contains(pageableDTO.getKeyword()))
                .sorted(Comparator.comparing(DscPublicFile::getName))
                .skip((pageableDTO.getPageIndex() - 1) * pageableDTO.getPageSize())
                .limit(pageableDTO.getPageSize())
                .collect(Collectors.toList());
        PageInfo<DscPublicFile> pageInfo = new PageInfo<>(dscPublicFileList, fsListNoLimit.size(), (int) Math.ceil(fsListNoLimit.size() / pageableDTO.getPageSize()));
        return CommonResult.success(pageInfo, "获取成功！");
    }


    @Override
    public CommonResult<String> deleteService(String serviceId) {
        Optional<DscPublicService> byId = dscPublicServiceDAO.findById(serviceId);
        if (!byId.isPresent()) CommonResult.failed("不存在此公共服务！");
        String serviceType = byId.get().getServiceType();
        // 区分矢量栅格
        if ("vector".equals(serviceType) || "geojson".equals(serviceType)) {
            Optional<DscVectorServiceInfo> byIdVec = dscVectorSDAO.findById(serviceId);
            if (!byIdVec.isPresent()) CommonResult.failed("服务信息不存在！");
            // 更新引用次数
            DscVectorServiceInfo dscVectorServiceInfo = byIdVec.get();
            dscVectorServiceInfo.setOwnerCount(dscVectorServiceInfo.getOwnerCount() - 1);
            dscVectorSDAO.save(dscVectorServiceInfo);
        } else {
            Optional<DscRasterService> byIdRas = dscRasterSDAO.findById(serviceId);
            if (!byIdRas.isPresent()) CommonResult.failed("服务信息不存在！");
            // 更新引用次数
            DscRasterService dscRasterService = byIdRas.get();
            dscRasterService.setOwnerCount(dscRasterService.getOwnerCount() - 1);
            dscRasterSDAO.save(dscRasterService);
        }
        // 删除公共服务记录
        dscPublicServiceDAO.delete(byId.get());
        return CommonResult.success("删除成功！");
    }

}
