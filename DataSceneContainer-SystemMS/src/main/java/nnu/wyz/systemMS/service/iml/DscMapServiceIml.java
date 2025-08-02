package nnu.wyz.systemMS.service.iml;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.DscMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DscMapServiceIml implements DscMapService {

    @Autowired
    private DscMapDao dscMapDao;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private AmazonS3 amazonS3;

    @Override
    public CommonResult<PageInfo<DscMap>> getMapList(PageableDTO pageableDTO) {
        // String userId = pageableDTO.getCriteria();
        String keyword = pageableDTO.getKeyword();
        Integer pageIndex = pageableDTO.getPageIndex();
        Integer pageSize = pageableDTO.getPageSize();

        List<String> mapIds;
        mapIds = dscMapDao.findAll().stream().map(DscMap::getId).toList();

        List<DscMap> mapListNoLimit = mapIds.stream()
                .map(dscMapDao::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(info -> info.getName().contains(keyword)) // 根据关键词进行模糊匹配
                .sorted(Comparator.comparing(DscMap::getPublishTime).reversed()).toList();

        List<DscMap> mapList = mapListNoLimit
                .stream()
                .skip((long) (pageIndex - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        PageInfo<DscMap> dscMapPageInfo = new PageInfo<>(mapList, mapListNoLimit.size(), (int) Math.ceil((double) mapListNoLimit.size() / pageSize));
        return CommonResult.success(dscMapPageInfo, "获取战例底图列表成功！");
    }

    @Override
    public CommonResult<String> deleteMap(String mapId, String userId) {
        DscMap byMapIdAndUserId = dscMapDao.findDscMapById(mapId, userId);
        if (Objects.isNull(byMapIdAndUserId)) {
            return CommonResult.failed("底图不存在！");
        } else {
            dscMapDao.delete(byMapIdAndUserId);
        }

        Optional<DscMap> byId = dscMapDao.findById(mapId);
        if (byId.isPresent()){
            DscMap dscMap = byId.get();
            String thumbnail = dscMap.getThumbnail();
            //如果缩略图存在
            if (thumbnail != null && !thumbnail.isEmpty()) {
                int oKBegin = thumbnail.indexOf(userId);
                String objectKey = thumbnail.substring(oKBegin);
                //删除缩略图
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(minioConfig.getSceneThumbnailsBucket(), objectKey);
                amazonS3.deleteObject(deleteObjectRequest);
            }
            //删除map
            dscMapDao.delete(dscMap);
        }
        return CommonResult.success("删除战例底图成功！");
    }
}
