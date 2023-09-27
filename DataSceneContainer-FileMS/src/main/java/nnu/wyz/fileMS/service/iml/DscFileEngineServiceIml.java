package nnu.wyz.fileMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.model.v2.Result;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.fileMS.config.MongoTransactional;
import nnu.wyz.fileMS.dao.*;
import nnu.wyz.fileMS.model.dto.*;
import nnu.wyz.fileMS.model.entity.DscCatalog;
import nnu.wyz.fileMS.model.entity.DscFileInfo;
import nnu.wyz.fileMS.model.entity.DscMvtServiceInfo;
import nnu.wyz.fileMS.model.entity.DscUserMvtS;
import nnu.wyz.fileMS.model.param.InitTaskParam;
import nnu.wyz.fileMS.service.DscFileEngineService;
import nnu.wyz.fileMS.service.DscRemoteFileMS;
import nnu.wyz.fileMS.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/5 9:56
 */
@Service
@Slf4j
public class DscFileEngineServiceIml implements DscFileEngineService {

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;

    @Autowired
    private DscMvtServiceInfoDAO dscMvtServiceInfoDAO;

    @Autowired
    private DscUserMvtSDAO dscUserMvtSDAO;

    @Autowired
    private ShpProcessDAO shpProcessDAO;

    @Autowired
    private AmazonS3 amazonS3;

    @Autowired
    private DscRemoteFileMS dscRemoteFileMS;

    @Value("${fileSavePath}")
    private String fileRootPath;

    @Value("${unzipTempPath}")
    private String unzipTempPath;

    @Value("${shp2pgsql}")
    private String pgCmd;

    @Value("${pgPassword}")
    private String pgPassword;

    @Value("${gateway_ip}")
    private String gateway_ip;

    @Value("${gateway_port}")
    private String gateway_port;

    @Value("${spring.application.name}")
    private String msName;


    /**
     * 解压 → 调用systemMS服务: InitTask → s3上传 → 调用systemMS服务: create → 删除临时解压文件
     *  TODO: 分布式事务 Seata
     *
     * @param unzipFileDTO
     * @return
     */
    @Override
    public CommonResult<String> unzip(UnzipFileDTO unzipFileDTO) {
        String fileId = unzipFileDTO.getFileId();
        String userId = unzipFileDTO.getUserId();
        String catalogId = unzipFileDTO.getCatalogId();
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "文件不存在！");
        }

        DscFileInfo dscFileInfo = byId.get();
        String bucket = dscFileInfo.getBucketName();
        String objectKey = dscFileInfo.getObjectKey();
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, objectKey);
        S3Object s3Object = amazonS3.getObject(getObjectRequest);
        String contentType = s3Object.getObjectMetadata().getContentType();
        if (!contentType.equals("application/zip")) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "文件类型不支持解压！");
        }
        String fullPath = fileRootPath + bucket + "/" + objectKey; //拿到文件存储位置
        String unzipDirPath = MessageFormat.format(unzipTempPath, UUID.randomUUID());
        File unzipDir = new File(unzipDirPath);
        unzipDir.mkdirs();  //创建临时解压文件夹
        try {
            List<JSONObject> fileObjects = FileUtils.zipUncompress(fullPath, unzipDirPath);
            int successCount = 0;
            ArrayList<Map<String, String>> records = new ArrayList<>();
            for (JSONObject fileObject :
                    fileObjects) {
                String fileName = (String) fileObject.get("fileName");
                String fileNameWithoutSuffix = fileName.substring(0, fileName.lastIndexOf("."));
                String objectName = UUID.randomUUID().toString();
                boolean isRepeat = false;
                for (Map<String, String> record :
                        records) {
                    if (record.get("name").equals(fileNameWithoutSuffix)) {
                        isRepeat = true;
                        objectName = record.get("id");
                    }
                }
                if (!isRepeat) {
                    HashMap<String, String> record = new HashMap<>();
                    record.put("name", fileNameWithoutSuffix);
                    record.put("id", objectName);
                    records.add(record);
                }
                String path = (String) fileObject.get("path");
                InputStream fileInputStream = Files.newInputStream(Paths.get(path));
                File file = new File(path);
                Long fileSize = file.length();
                String md5 = DigestUtils.md5DigestAsHex(fileInputStream);
                InitTaskParam initTaskParam = new InitTaskParam();
                initTaskParam.setIdentifier(md5)
                        .setFileName(fileName)
                        .setTotalSize(fileSize)
                        .setChunkSize(fileSize)
                        .setObjectName(objectName)
                        .setUserId(userId);
                Result<TaskInfoDTO> taskInfoDTOResult = dscRemoteFileMS.initTask(initTaskParam);
                if (taskInfoDTOResult.getCode().equals(200000)) {
                    TaskInfoDTO data = taskInfoDTOResult.getData();
                    String bucketName = data.getTaskRecord().getBucketName();
                    String objectKey1 = data.getTaskRecord().getObjectKey();
                    String contentType2 = MediaTypeFactory.getMediaType(objectKey1).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
                    ObjectMetadata objectMetadata = new ObjectMetadata();
                    objectMetadata.setContentType(contentType2);
                    PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey1, file);
                    putObjectRequest.setMetadata(objectMetadata);
                    amazonS3.putObject(putObjectRequest);
                    UploadFileDTO uploadFileDTO = new UploadFileDTO();
                    uploadFileDTO.setUserId(userId)
                            .setTaskId(data.getTaskRecord().getId())
                            .setCatalogId(catalogId);
                    CommonResult<String> upload = dscRemoteFileMS.upload(uploadFileDTO);
                    if (upload.getCode() == 200) {
                        successCount++;
                    }
                }
            }
            //删除临时解压文件夹
            FileUtils.deleteDirectory(unzipDirPath);
            if (successCount >= fileObjects.size()) {
                return CommonResult.success("解压全部完成！");
            } else {
                return CommonResult.success("解压部分完成！");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @MongoTransactional
    public CommonResult<String> publishShapefile(PublishShapefileDTO publishShapefileDTO) {
        String userId = publishShapefileDTO.getUserId();
        String fileId = publishShapefileDTO.getFileId();
        String catalogId = publishShapefileDTO.getCatalogId();
        String srid = publishShapefileDTO.getSrid();
        String code = publishShapefileDTO.getCode();
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed("文件不存在");
        }
        DscUserMvtS isExist = dscUserMvtSDAO.findByUserIdAndMvtSName(userId, publishShapefileDTO.getName());
        if (!Objects.isNull(isExist)) {
            return CommonResult.failed("服务名不能重复，请更改发布服务的名称！");
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
                DscMvtServiceInfo dscMvtServiceInfo = new DscMvtServiceInfo();
                String mvtId = IdUtil.objectId();
                dscMvtServiceInfo.setId(mvtId)
                        .setPublisher(userId)
                        .setFileId(fileId)
                        .setMvtName(publishShapefileDTO.getName())
                        .setMvtUrl("http://" + gateway_ip + ":" + gateway_port + "/" + msName + "/dsc-mvtS/mvt/" + ptName + "/{z}/{x}/{y}.pbf")
                        .setPtName(ptName)
                        .setOwnerCount(1L)
                        .setPublishTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"))
                        .setType("vector")
                        .setGeoType(geoType)
                        .setBbox(shpBox2D)
                        .setCenter(center);
                dscMvtServiceInfoDAO.insert(dscMvtServiceInfo);
                DscUserMvtS dscUserMvtS = new DscUserMvtS();
                dscUserMvtS.setId(IdUtil.objectId())
                        .setUserId(userId)
                        .setMvtSName(publishShapefileDTO.getName())
                        .setMvtId(mvtId);
                dscUserMvtSDAO.insert(dscUserMvtS);
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


}
