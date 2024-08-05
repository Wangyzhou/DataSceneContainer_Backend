package nnu.wyz.systemMS.task;

import lombok.extern.slf4j.Slf4j;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscRasterSDAO;
import nnu.wyz.systemMS.dao.DscVectorSDAO;
import nnu.wyz.systemMS.dao.ShpProcessDAO;
import nnu.wyz.systemMS.model.entity.DscFileInfo;
import nnu.wyz.systemMS.model.entity.DscRasterService;
import nnu.wyz.systemMS.model.entity.DscVectorServiceInfo;
import nnu.wyz.systemMS.service.DscVectorSService;
import nnu.wyz.systemMS.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Desription：用于清理被删除记录且没有任何场景引用的服务信息，删除服务快照文件以及与原文件解绑（publishCount-1)
 * @Author：mfz
 * @Date：2024/8/3 16:27
 */

@Component
@Slf4j
public class DeleteServiceTask {

    @Autowired
    DscVectorSDAO dscVectorSDAO;

    @Autowired
    DscRasterSDAO dscRasterSDAO;

    @Autowired
    DscFileDAO dscFileDAO;

    @Autowired
    private ShpProcessDAO shpProcessDAO;

    @Autowired
    private MinioConfig minioConfig;

    @Value("${fileSavePath}")
    private String rootPath;

    // 需要在删除文件的定时任务之前
    @Scheduled(cron = "0 0 5 * * ?")
    public void deleteService() {
        log.info("************删除服务定时任务开始执行************");
        // 清理ownerCount为0的服务资源对应的物理资源（不做直接删除，直接删除交给删除文件定时任务，这里只做类似文件ownerCount-1的操作），同时将服务对应的file对象的发布次数-1

        // 矢量
        List<DscVectorServiceInfo> allByOwnerCountVec = dscVectorSDAO.findAllByOwnerCount(0L);
        dscVectorSDAO.deleteAll(allByOwnerCountVec);
        // 删除源服务（pg表）
        allByOwnerCountVec.stream().filter(dscVectorServiceInfo -> "vector".equals(dscVectorServiceInfo.getType())).forEach(dscVectorServiceInfo -> {
            Boolean isDelete = shpProcessDAO.deletePgTable(dscVectorServiceInfo.getPtName());
            if (!isDelete) {
                throw new RuntimeException("pg表删除失败");
            }
        });

        // 栅格
        List<DscRasterService> allByOwnerCountRas = dscRasterSDAO.findAllByOwnerCount(0L);
        dscRasterSDAO.deleteAll(allByOwnerCountRas);
        // 删除源服务
        // 删除tif服务的初始png快照文件
        List<String> fileIdCollectPng = allByOwnerCountRas.stream().filter(dscRasterService -> "image".equals(dscRasterService.getType()) && !Objects.isNull(dscRasterService.getOriFileId())).map(DscRasterService::getFileId).collect(Collectors.toList());
        List<DscFileInfo> pngFileInfos = dscFileDAO.findAllByIds(fileIdCollectPng);
        List<DscFileInfo> newPngFileInfos = pngFileInfos.stream().map(dscFileInfo -> {
            dscFileInfo.setOwnerCount(dscFileInfo.getOwnerCount() - 1);
            return dscFileInfo;
        }).collect(Collectors.toList());
        dscFileDAO.saveAll(newPngFileInfos);
        log.info("tif服务快照删除成功,共删除" + pngFileInfos.size() + "个快照");
        // 删除tiles服务的瓦片文件
        allByOwnerCountRas.stream().filter(dscRasterService -> "tiles".equals(dscRasterService.getType())).forEach(dscRasterService -> {
            String tilesDirPath = rootPath + minioConfig.getRasterTilesBucket() + File.separator + dscRasterService.getPublisher() + File.separator + dscRasterService.getId();
            FileUtils.deleteDirectory(tilesDirPath);
        });


        // 更新服务对应原始file的publishCount信息，告知当前服务已经不再受这个原文件影响，可以物理删（如果没有发布其他服务）
        // 矢量服务的原始文件id集合
        List<String> fileIdCollectVec = allByOwnerCountVec.stream().map(DscVectorServiceInfo::getFileId).collect(Collectors.toList());
        // 栅格服务的原始文件id集合
        List<String> fileIdCollectRas = allByOwnerCountRas.stream().map(dscRasterService -> {
            if ("image".equals(dscRasterService.getType()) && !Objects.isNull(dscRasterService.getOriFileId())) {
                return dscRasterService.getOriFileId();
            } else {
                return dscRasterService.getFileId();
            }
        }).collect(Collectors.toList());
        // 合并id集合
        List<String> allFileIds = Stream.concat(fileIdCollectVec.stream(), fileIdCollectRas.stream()).collect(Collectors.toList());
        List<DscFileInfo> allByIds = dscFileDAO.findAllByIds(allFileIds);
        // publishCount-1
        List<DscFileInfo> fileInfos = allByIds.stream().map(dscFileInfo -> {
            dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() - 1);
            return dscFileInfo;
        }).collect(Collectors.toList());
        dscFileDAO.saveAll(fileInfos);
        log.info("服务删除完成，共删除" + allByIds.size() + "个服务");
        log.info("************删除服务定时任务执行结束************");
    }
}
