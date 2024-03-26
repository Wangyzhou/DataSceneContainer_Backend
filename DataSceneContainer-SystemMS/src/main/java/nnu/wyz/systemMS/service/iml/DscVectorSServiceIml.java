package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.PageableDTO;
import nnu.wyz.systemMS.model.dto.PublishGeoJSONDTO;
import nnu.wyz.systemMS.model.dto.PublishShapefileDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.DscMvtService;
import nnu.wyz.systemMS.service.DscVectorSService;
import nnu.wyz.systemMS.utils.GeoJSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/10/11 16:10
 */
@Service
@Slf4j
public class DscVectorSServiceIml implements DscVectorSService {

    @Autowired
    private DscMvtService dscMvtService;
    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;

    @Autowired
    private DscVectorSDAO dscVectorSDAO;
    @Autowired
    private DscUserVectorSDAO dscUserVectorSDAO;

    @Autowired
    private ShpProcessDAO shpProcessDAO;

    @Autowired
    private MinioConfig minioConfig;
    @Value("${fileSavePath}")
    private String fileRootPath;

    @Value("${fileSavePathWin}")
    private String fileRootPathWin;

    @Value("${shp2pgsql}")
    private String pgCmd;

    @Value("${shp2pgsqlWin}")
    private String pgCmdWin;

    @Value("${pg_password}")
    private String pgPassword;

    @Value("${mvt_url}")
    private String mvtUrl;

    @Value("${spring.application.name}")
    private String msName;

    @Override
    public CommonResult<String> publishShp2VectorS(PublishShapefileDTO publishShapefileDTO) {
        if(Pattern.matches("[0-9].*", publishShapefileDTO.getName())) {
            return CommonResult.failed("服务名称不能以数字开头");
        }
        String userId = publishShapefileDTO.getUserId();
        String fileId = publishShapefileDTO.getFileId();
        String catalogId = publishShapefileDTO.getCatalogId();
        String srid = publishShapefileDTO.getSrid();
        String code = publishShapefileDTO.getCode();
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed("文件不存在");
        }
        DscUserVectorS isExist = dscUserVectorSDAO.findDscUserVectorSByUserIdAndVectorSNameAndVectorSType(userId, publishShapefileDTO.getName(), "vector");
        if (!Objects.isNull(isExist)) {
            return CommonResult.failed("存在名称相同的MVT服务，请更改发布服务的名称！");
        }
        DscFileInfo dscFileInfo = byId.get();
        String fileName = dscFileInfo.getFileName();
        String fileNameWithoutSuffix = fileName.substring(0, fileName.lastIndexOf("."));
        //检查有无.shx .dbf .prj等文件
        Optional<DscCatalog> dscCatalogDAOById = dscCatalogDAO.findById(catalogId);
        if (!dscCatalogDAOById.isPresent()) {
            return CommonResult.failed("目录不存在");
        }
        DscCatalog dscCatalog = dscCatalogDAOById.get();
        List<CatalogChildrenDTO> children = dscCatalog.getChildren();
        ArrayList<String> shapefiles = new ArrayList<>();
        for (CatalogChildrenDTO next : children) {
            String name = next.getName();
            if (name.equals(fileNameWithoutSuffix + ".shp") || name.equals(fileNameWithoutSuffix + ".shx")
                    || name.equals(fileNameWithoutSuffix + ".dbf")) {
                shapefiles.add(name);
            }
        }
        if (!shapefiles.contains(fileNameWithoutSuffix + ".shp") || !shapefiles.contains(fileNameWithoutSuffix + ".shx")
                || !shapefiles.contains(fileNameWithoutSuffix + ".dbf")) {
            return CommonResult.failed("发布失败，组成Shapefile的.shp、.shx、.dbf必要文件不完整！");
        }
        String fileRoot = System.getProperty("os.name").startsWith("Windows") ? fileRootPathWin : fileRootPath;
        String separator = File.separator;
        String fullPath = fileRoot + dscFileInfo.getBucketName() + separator + dscFileInfo.getObjectKey();
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(code);
        //指定cpg文件
        if (byId1.isPresent()) {
            DscFileInfo cpgFile = byId1.get();
            String cpgFilePath = fileRoot + cpgFile.getBucketName() + separator + cpgFile.getObjectKey();
            try {
                FileInputStream fileInputStream = new FileInputStream(cpgFilePath);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                code = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return CommonResult.failed("读取cpg文件失败");
            }
        }
        String ptName = (publishShapefileDTO.getName() + "_" + IdUtil.objectId()).replace(" ", "_");
        String pgCmdStr = System.getProperty("os.name").startsWith("Windows") ? MessageFormat.format(pgCmdWin, srid, code, fullPath, ptName) : MessageFormat.format(pgCmd, srid, code, fullPath, ptName);
        Process pro;
        ProcessBuilder processBuilder = new ProcessBuilder();
        BufferedReader bf;
        try {
            Map<String, String> environment = processBuilder.environment();
            environment.remove("PGPASSWORD");
            environment.put("PGPASSWORD", pgPassword);
            if (System.getProperty("os.name").startsWith("Windows")) {
                processBuilder.command("cmd", "/c", pgCmdStr);
            } else {
                processBuilder.command("/bin/sh", "-c", pgCmdStr);
            }
            pro = processBuilder.start();
            bf = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = bf.readLine()) != null) {
                output.append(line);
            }
            if (output.toString().contains("COMMIT") && output.toString().contains("CREATE INDEX") && output.toString().contains("ANALYZE")) {
                log.info("文件" + fileName + "成功存入PG！");
                String geoType = shpProcessDAO.getShpType(ptName);
                List<Double> shpBox2D = shpProcessDAO.getShpBox2D(ptName);
                Double WLng = shpBox2D.get(0);
                Double ELng = shpBox2D.get(2);
                Double SLat = shpBox2D.get(1);
                Double NLat = shpBox2D.get(3);
                ArrayList<Double> center = new ArrayList<>();
                center.add((WLng + ELng) / 2);
                center.add((SLat + NLat) / 2);
                DscVectorServiceInfo dscVectorServiceInfo = new DscVectorServiceInfo();
                String mvtId = IdUtil.objectId();
                if(mvtUrl.endsWith("/")){
                    mvtUrl = mvtUrl.substring(0,mvtUrl.length()-1);
                }
                dscVectorServiceInfo.setId(mvtId)
                        .setName(publishShapefileDTO.getName())
                        .setUrl(mvtUrl + "/" + msName + "/dsc-vector-service/getMvt/" + ptName + "/{z}/{x}/{y}.pbf")
                        .setPublisher(userId)
                        .setPublishTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                        .setFileId(fileId)
                        .setPtName(ptName)
                        .setType("vector")
                        .setCenter(center)
                        .setBbox(shpBox2D)
                        .setOwnerCount(1L)
                        .setGeoType(geoType);
                dscVectorSDAO.insert(dscVectorServiceInfo);
                DscUserVectorS dscUserVectorS = new DscUserVectorS();
                dscUserVectorS.setId(IdUtil.objectId())
                        .setUserId(userId)
                        .setVectorSName(publishShapefileDTO.getName())
                        .setVectorSId(mvtId)
                        .setVectorSType("vector");
                dscUserVectorSDAO.insert(dscUserVectorS);
                dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() + 1L);
                dscFileDAO.save(dscFileInfo);
                return CommonResult.success("发布成功！");
            }
            return CommonResult.failed("发布失败！");
        } catch (IOException e) {
            e.printStackTrace();
            return CommonResult.failed("发布失败！");
        }
    }

    @Override
    public void getMvt(int zoom, int x, int y, String tableName, HttpServletResponse response) {
        dscMvtService.getMvt(zoom, x, y, tableName, response);
    }

    @Override
    public CommonResult<PageInfo<DscVectorServiceInfo>> getVectorServiceList(PageableDTO pageableDTO) {
        String userId = pageableDTO.getCriteria();
        String keyword = pageableDTO.getKeyword(); // 新增关键词参数
        Integer pageIndex = pageableDTO.getPageIndex();
        Integer pageSize = pageableDTO.getPageSize();
        List<DscVectorServiceInfo> vsListNoLimit = dscUserVectorSDAO.findAllByUserId(userId)
                .stream()
                .map(DscUserVectorS::getVectorSId)
                .map(dscVectorSDAO::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(info -> info.getName().contains(keyword)) // 根据关键词进行模糊匹配
                .sorted(Comparator.comparing(DscVectorServiceInfo::getName))
                .collect(Collectors.toList());
        List<DscVectorServiceInfo> dscVectorServiceInfos = vsListNoLimit
                .stream()
                .skip((long) (pageIndex - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        PageInfo<DscVectorServiceInfo> pageInfo = new PageInfo<>(dscVectorServiceInfos, vsListNoLimit.size(), (int) Math.ceil((double) vsListNoLimit.size() / pageSize));
        return CommonResult.success(pageInfo, "获取成功！");
    }

    @Override
    public CommonResult<String> deleteVectorService(String userId, String vectorSId) {
        Optional<DscVectorServiceInfo> byId = dscVectorSDAO.findById(vectorSId);
        if (!byId.isPresent()) {
            return CommonResult.failed("服务不存在！");
        }
        DscUserVectorS dscUserVectorS = dscUserVectorSDAO.findByUserIdAndVectorSId(userId, vectorSId);
        if (Objects.isNull(dscUserVectorS)) {
            return CommonResult.failed("用户未拥有该服务！");
        }
        dscUserVectorSDAO.delete(dscUserVectorS);
        DscVectorServiceInfo dscVectorServiceInfo = byId.get();
        dscVectorServiceInfo.setOwnerCount(dscVectorServiceInfo.getOwnerCount() - 1);
        dscVectorSDAO.save(dscVectorServiceInfo);
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(dscVectorServiceInfo.getFileId());
        if (byId1.isPresent()) {
            DscFileInfo dscFileInfo = byId1.get();
            dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() - 1);
            dscFileDAO.save(dscFileInfo);
        }
        if (dscVectorServiceInfo.getOwnerCount() == 0) {
            //删除源服务
            if (dscVectorServiceInfo.getType().equals("vector")) {
                Boolean isDelete = shpProcessDAO.deletePgTable(dscVectorServiceInfo.getPtName());
                if (!isDelete) {
                    throw new RuntimeException("pg表删除失败");
                }
            }
            dscVectorSDAO.delete(dscVectorServiceInfo);
        }
        return CommonResult.success("删除成功！");
    }

    @Override
    public CommonResult<List<DscVectorServiceInfo>> getVectorServicesByFileId(String fileId) {
        List<DscVectorServiceInfo> allByFileId = dscVectorSDAO.findAllByFileId(fileId);
        return CommonResult.success(allByFileId, "获取成功！");
    }

    @Override
    public CommonResult<String> publishGeoJSON2VectorS(PublishGeoJSONDTO publishGeoJSONDTO) {
        String userId = publishGeoJSONDTO.getUserId();
        String fileId = publishGeoJSONDTO.getFileId();
        String name = publishGeoJSONDTO.getName();
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed("文件不存在！");
        }
        DscUserVectorS isExist = dscUserVectorSDAO.findDscUserVectorSByUserIdAndVectorSNameAndVectorSType(userId, name, "geojson");
        if (!Objects.isNull(isExist)) {
            return CommonResult.failed("存在名称相同的GeoJSON服务，请更改发布服务的名称！");
        }
        DscFileInfo dscFileInfo = byId.get();
        //解析GeoJSON，判断是否可以发布
        String fileRoot = System.getProperty("os.name").startsWith("Windows") ? fileRootPathWin : fileRootPath;
        String separator = File.separator;
        String fullPath = fileRoot + dscFileInfo.getBucketName() + separator + dscFileInfo.getObjectKey();
        final CommonResult<String> initialResult = GeoJSONUtil.initUtil(fullPath);
        if (initialResult.getCode() != 200) {
            return initialResult;
        }
        String geoJSONType = GeoJSONUtil.getGeoJSONType();
        if ("Unknown".equals(geoJSONType)) {
            return CommonResult.failed("暂不支持跨类型要素集合，请确保该GeoJSON只是点、线、面其中一种！");
        }
        List<Double> geoJSONBBOX;
        List<Double> center;
        try {
            geoJSONBBOX = GeoJSONUtil.getGeoJSONBBOX();
            center = GeoJSONUtil.getCenterFromBBOX(geoJSONBBOX);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResult.failed("解析GeoJSON失败");
        }
        String endpoint = minioConfig.getEndpoint();
        String bucketName = dscFileInfo.getBucketName();
        String objectKey = dscFileInfo.getObjectKey();
        String serviceUrl = endpoint + "/" + bucketName + "/" + objectKey;
        DscVectorServiceInfo dscVectorServiceInfo = new DscVectorServiceInfo();
        String serviceId = IdUtil.objectId();
        dscVectorServiceInfo.setId(serviceId)
                .setName(name)
                .setUrl(serviceUrl)
                .setPublisher(userId)
                .setOwnerCount(1L)
                .setType("geojson")
                .setFileId(fileId)
                .setGeoType(geoJSONType.toUpperCase())
                .setBbox(geoJSONBBOX)
                .setCenter(center)
                .setPublishTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscVectorSDAO.insert(dscVectorServiceInfo);
        DscUserVectorS dscUserVectorS = new DscUserVectorS();
        dscUserVectorS.setId(IdUtil.objectId())
                .setUserId(userId)
                .setVectorSId(serviceId)
                .setVectorSName(name)
                .setVectorSType("geojson");
        dscUserVectorSDAO.insert(dscUserVectorS);
        //增加文件发布记录
        dscFileInfo.setPublishCount(dscFileInfo.getPublishCount() + 1);
        dscFileDAO.save(dscFileInfo);
        return CommonResult.success("发布成功！");
    }

    @Override
    public CommonResult<List<DscVectorServiceInfo>> getVectorServiceListByFileId(String fileId) {
        List<DscVectorServiceInfo> allByFileId = dscVectorSDAO.findAllByFileId(fileId);
        return CommonResult.success(allByFileId, "获取成功!");
    }
}
