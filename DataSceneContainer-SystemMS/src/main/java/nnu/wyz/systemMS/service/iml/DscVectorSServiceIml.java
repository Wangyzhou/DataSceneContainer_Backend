package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import nnu.wyz.systemMS.model.dto.PublishGeoJSONDTO;
import nnu.wyz.systemMS.model.dto.PublishShapefileDTO;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.service.DscVectorSService;
import nnu.wyz.systemMS.utils.GeoJSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;

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

    @Value("${shp2pgsql}")
    private String pgCmd;

    @Value("${pg_password}")
    private String pgPassword;

    @Value("${gateway_addr}")
    private String gateway_addr;

    @Value("${spring.application.name}")
    private String msName;

    @Override
    public CommonResult<String> publishShp2VectorS(PublishShapefileDTO publishShapefileDTO) {
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
        String fullPath = fileRootPath + dscFileInfo.getBucketName() + "/" + dscFileInfo.getObjectKey();
        Optional<DscFileInfo> byId1 = dscFileDAO.findById(code);
        if (byId1.isPresent()) {
            DscFileInfo cpgFile = byId1.get();
            String cpgFilePath = fileRootPath + cpgFile.getBucketName() + "/" + cpgFile.getObjectKey();
            try {
                FileInputStream fileInputStream = new FileInputStream(cpgFilePath);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                code = bufferedReader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String ptNamePrefix = fileName.substring(0, fileName.lastIndexOf("."));
        String ptName = ptNamePrefix + "_" + IdUtil.objectId();
        String shp2pgsqlCmd = MessageFormat.format(pgCmd, srid, code, fullPath, ptName);
        Process pro;
        ProcessBuilder processBuilder = new ProcessBuilder();
        BufferedReader bf;
        try {
            Map<String, String> environment = processBuilder.environment();
            environment.remove("PGPASSWORD");
            environment.put("PGPASSWORD", pgPassword);
            if (System.getProperty("os.name").startsWith("Windows")) {
                processBuilder.command("cmd", "/c", shp2pgsqlCmd);
            } else {
                processBuilder.command("/bin/sh", "-c", shp2pgsqlCmd);
            }
            pro = processBuilder.start();
            bf = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = bf.readLine()) != null) {
                log.info(line);
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
                dscVectorServiceInfo.setId(mvtId)
                        .setName(publishShapefileDTO.getName())
                        .setUrl(gateway_addr + "/" + msName + "/dsc-vector-service/getMvt/" + ptName + "/{z}/{x}/{y}.pbf")
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
    public CommonResult<List<DscVectorServiceInfo>> getVectorServiceList(String userId) {
        List<DscUserVectorS> allByUserId = dscUserVectorSDAO.findAllByUserId(userId);
        Iterator<DscUserVectorS> iterator = allByUserId.iterator();
        List<DscVectorServiceInfo> dscVectorServiceInfos = new ArrayList<>();
        while (iterator.hasNext()) {
            DscUserVectorS dscUserVectorS = iterator.next();
            Optional<DscVectorServiceInfo> byId = dscVectorSDAO.findById(dscUserVectorS.getVectorSId());
            if (!byId.isPresent()) {
                continue;
            }
            DscVectorServiceInfo dscVectorServiceInfo = byId.get();
            dscVectorServiceInfos.add(dscVectorServiceInfo);
        }
        return CommonResult.success(dscVectorServiceInfos, "获取成功！");
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
        String fullPath = fileRootPath + dscFileInfo.getBucketName() + "/" + dscFileInfo.getObjectKey();
        final CommonResult<String> initialResult = GeoJSONUtil.initUtil(fullPath);
        if (initialResult.getCode() != 200) {
            return initialResult;
        }
        String geoJSONType = GeoJSONUtil.getGeoJSONType();
        List<Double> geoJSONBBOX = GeoJSONUtil.getGeoJSONBBOX(geoJSONType);
        List<Double> center = GeoJSONUtil.getCenterFromBBOX(geoJSONBBOX);
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
}
