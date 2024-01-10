package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.dao.*;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.model.param.InitTaskParam;
import nnu.wyz.systemMS.websocket.WebSocketServer;
import nnu.wyz.systemMS.service.DscFileService;

import nnu.wyz.systemMS.service.SysUploadTaskService;
import nnu.wyz.systemMS.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 16:24
 */
@Service
@Slf4j
public class DscFileServiceIml implements DscFileService {

    @Resource
    private AmazonS3 amazonS3;

    @Autowired
    private SysUploadTaskDAO sysUploadTaskDAO;

    @Autowired
    private DscFileDAO dscFileDAO;

    @Autowired
    private DscCatalogDAO dscCatalogDAO;

    @Autowired
    private DscUserDAO dscUserDAO;

    @Autowired
    private SysUploadTaskService sysUploadTaskService;

    @Autowired
    private MinioConfig minioConfig;

    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${fileSavePath}")
    private String fileRootPath;

    @Value("${fileSavePathWin}")
    private String fileRootPathWin;

    @Value("${unzipTempPath}")
    private String unzipTempPath;

    @Value("${unzipTempPathWin}")
    private String unzipTempPathWin;

    /**
     * 文件上传，文件可以重复上传，但同一目录下不能有同名文件
     * 当前目录不能有重名文件，整个用户空间不能有两份相同资源
     *
     * @param uploadFileDTO
     * @return
     */
    @Override
    public CommonResult<String> create(UploadFileDTO uploadFileDTO) {
        String userId = uploadFileDTO.getUserId();
        String taskId = uploadFileDTO.getTaskId();
        String catalogId = uploadFileDTO.getCatalogId();
        Optional<SysUploadTask> sysUploadTaskDAOById = sysUploadTaskDAO.findById(taskId);
        if (!sysUploadTaskDAOById.isPresent()) {
            return CommonResult.failed("上传任务不存在");
        }
        SysUploadTask task = sysUploadTaskDAOById.get();
        String fileId = task.getFileId();
        Optional<DscCatalog> byId = dscCatalogDAO.findById(catalogId);
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到载体目录!");
        }
        DscCatalog dscCatalog = byId.get();
        List<CatalogChildrenDTO> children = dscCatalog.getChildren();
        for (CatalogChildrenDTO next : children) {  //判断该目录下是否有同名文件或相同文件，即判断上传环境
            //孩子节点不为folder且文件名出现冲突
            if (!next.getType().equals("folder") && next.getName().equals(task.getFileName())) {
                return CommonResult.failed(ResultCode.VALIDATE_FAILED, "在该目录下存在同名文件，请更改文件名或更换文件夹进行上传！");
            }
            if (next.getId().equals(fileId)) {
                return CommonResult.failed(ResultCode.VALIDATE_FAILED, "在该目录下存在相同文件，请更换文件夹进行上传！");
            }
        }
        if (fileId != null) {  //说明用户一天内上传过该文件
            String dateTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
            Optional<DscFileInfo> dscFileDAOById = dscFileDAO.findById(fileId);
            DscFileInfo dscFileInfo = dscFileDAOById.get();
            String fileName = dscFileInfo.getFileName();
            CatalogChildrenDTO childrenDTO = new CatalogChildrenDTO();
            childrenDTO.setId(fileId)
                    .setName(fileName)
                    .setType(dscFileInfo.getFileSuffix())
                    .setSize(dscFileInfo.getSize())
                    .setUpdatedTime(dateTime);
            dscCatalog.getChildren().add(childrenDTO);
            dscCatalog.setTotal(dscCatalog.getTotal() + 1);
            dscCatalog.setUpdatedTime(dateTime);
            dscCatalogDAO.save(dscCatalog);
            dscFileInfo.setOwnerCount(dscFileInfo.getOwnerCount() + 1);
            dscFileDAO.save(dscFileInfo);
            return CommonResult.success("文件：" + fileName + "上传成功！");
        }
        //若用户未上传过该文件，则创建该文件记录并更新目录
        GetObjectRequest getObjectRequest = new GetObjectRequest(task.getBucketName(), task.getObjectKey());
        S3Object s3Object = amazonS3.getObject(getObjectRequest);
        ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
        long size = objectMetadata.getContentLength();
        String fileName = task.getFileName();
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
        DscFileInfo dscFileInfo = new DscFileInfo();
        String dateTime = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        fileId = IdUtil.objectId();
        task.setFileId(fileId);     //任务实体中保存该文件的ID，方便之后文件删除时删除任务
        sysUploadTaskDAO.save(task);
        dscFileInfo
                .setId(fileId)
                .setMd5(task.getFileIdentifier())
                .setFileName(fileName)
                .setFileSuffix(ext)
                .setCreatedUser(userId)
                .setSize(size)
                .setCreatedTime(dateTime)
                .setUpdatedTime(dateTime)
                .setPreviewCount(0L)
                .setDownloadCount(0L)
                .setOwnerCount(1L)
                .setPublishCount(0L)
                .setBucketName(task.getBucketName())
                .setObjectKey(task.getObjectKey());
        dscFileDAO.insert(dscFileInfo);
        CatalogChildrenDTO childrenDTO = new CatalogChildrenDTO();
        childrenDTO.setId(fileId)
                .setName(fileName)
                .setType(ext)
                .setSize(size)
                .setUpdatedTime(dateTime);
        dscCatalog.getChildren().add(childrenDTO);
        dscCatalog.setTotal(dscCatalog.getTotal() + 1);
        dscCatalog.setUpdatedTime(dateTime);
        dscCatalogDAO.save(dscCatalog);
        return CommonResult.success("文件：" + fileName + "上传成功！");
    }


    /**
     * 文件删除
     *
     * @param deleteFileDTO
     * @return
     */
    @Override
    public CommonResult<String> delete(DeleteFileDTO deleteFileDTO) {
        String fileId = deleteFileDTO.getFileId();
        String catalogId = deleteFileDTO.getCatalogId();
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed("文件不存在！");
        }
        DscFileInfo dscFileInfo = byId.get();
        dscFileInfo.setOwnerCount(dscFileInfo.getOwnerCount() - 1);     //文件拥有者数 - 1
        dscFileDAO.save(dscFileInfo);
        //父目录孩子节点的摘除，更新父目录
        DscCatalog dscCatalog = dscCatalogDAO.findById(catalogId).get();
        List<CatalogChildrenDTO> children = dscCatalog.getChildren();
        Iterator<CatalogChildrenDTO> iterator = children.iterator();
        while (iterator.hasNext()) {
            CatalogChildrenDTO temp = iterator.next();
            if (temp.getId().equals(fileId)) {
                iterator.remove();
                break;
            }
        }
        dscCatalog.setTotal(dscCatalog.getTotal() - 1);
        dscCatalog.setUpdatedTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        dscCatalogDAO.save(dscCatalog);
        return CommonResult.success("删除成功!");
    }

    /**
     * 文件下载
     *
     * @param response
     */
    @Override
    public void download(String fileId, HttpServletResponse response) {
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            response.setContentType("application/json");
            try {
                response.getWriter().print(CommonResult.failed("未找到该文件！"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        DscFileInfo dscFileInfo = byId.get();
        dscFileInfo.setDownloadCount(dscFileInfo.getDownloadCount() + 1);
        dscFileDAO.save(dscFileInfo);
        String bucketName = dscFileInfo.getBucketName();
        String objectKey = dscFileInfo.getObjectKey();
        String fileRoot = System.getProperty("os.name").startsWith("Windows") ? fileRootPathWin : fileRootPath;
        String separator = File.separator;
        String fullPath = fileRoot + bucketName + separator + objectKey;
        BufferedInputStream bufferedInputStream;
        OutputStream out;
        try {
            bufferedInputStream = new BufferedInputStream(Files.newInputStream(Paths.get(fullPath)));
            response.setContentType("application/octet-stream");
            out = response.getOutputStream();
            byte[] buf = new byte[1024 * 8];
            int len;
            while ((len = bufferedInputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            response.flushBuffer();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取文件详情
     *
     * @param fileId
     * @return
     */
    @Override
    public CommonResult<JSONObject> getFileInfo(String fileId) {
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "未找到此文件！");
        }
        DscFileInfo dscFileInfo = byId.get();
        HashMap<String, Object> fileInfoMap = new HashMap<>();
        fileInfoMap.put("id", dscFileInfo.getId());
        fileInfoMap.put("name", dscFileInfo.getFileName());
        fileInfoMap.put("type", dscFileInfo.getFileSuffix());
        fileInfoMap.put("size", dscFileInfo.getSize());
        fileInfoMap.put("previewCount", dscFileInfo.getPreviewCount());
        fileInfoMap.put("downloadCount", dscFileInfo.getDownloadCount());
        fileInfoMap.put("publishCount", dscFileInfo.getPublishCount());
        fileInfoMap.put("ownerCount", dscFileInfo.getOwnerCount());
        fileInfoMap.put("createdTime", dscFileInfo.getCreatedTime());
        fileInfoMap.put("updatedTime", dscFileInfo.getUpdatedTime());
        JSONObject fileInfo = new JSONObject(fileInfoMap);
        return CommonResult.success(fileInfo);
    }

    /**
     * 获取文件预览url
     *
     * @param fileId
     * @return
     */
    @Override
    public CommonResult<String> getFilePreviewUrl(String fileId) {
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "未找到该文件！");
        }
        DscFileInfo dscFileInfo = byId.get();
        dscFileInfo.setPreviewCount(dscFileInfo.getPreviewCount() + 1);
        dscFileDAO.save(dscFileInfo);
        return CommonResult.success(minioConfig.getEndpoint() + "/" + dscFileInfo.getBucketName() + "/" + dscFileInfo.getObjectKey(), "获取预览地址成功！");
    }

    @Override
    public CommonResult<String> shareFile(FileShareDTO fileShareDTO) {
        String fromUserEmail = fileShareDTO.getFromUser();
        String sourceId = fileShareDTO.getFileId();
        List<String> toUsers = fileShareDTO.getToUsers();
        DscUser fromDscUser = dscUserDAO.findDscUserByEmail(fromUserEmail);
        toUsers.forEach(toUser -> {
            Message message = new Message();
            message.setFrom(fromUserEmail)
                    .setTo(toUser)
                    .setTopic("文件分享")
                    .setResource(sourceId)
                    .setType("fileMsg")
                    .setIsRead(false)
                    .setText(MessageFormat.format("用户{0}给您分享了一份文件，请查收！", fromDscUser.getUserName()));
            webSocketServer.sendInfo(toUser, JSON.toJSONString(message));
        });
        return CommonResult.success("消息发送成功！");
    }

    @Override
    public CommonResult<String> importResource(FileShareImportDTO fileShareImportDTO) {
        String fileId = fileShareImportDTO.getFileId();
        String catalogId = fileShareImportDTO.getCatalogId();
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed("意料之外的错误！");
        }
        DscFileInfo dscFileInfo = byId.get();
        dscFileInfo.setOwnerCount(dscFileInfo.getOwnerCount() + 1);
        dscFileDAO.save(dscFileInfo);
        CatalogChildrenDTO catalogChildrenDTO = new CatalogChildrenDTO();
        catalogChildrenDTO.setId(dscFileInfo.getId())
                .setName(dscFileInfo.getFileName())
                .setType(dscFileInfo.getFileSuffix())
                .setSize(dscFileInfo.getSize())
                .setUpdatedTime(dscFileInfo.getUpdatedTime());
        Optional<DscCatalog> byId1 = dscCatalogDAO.findById(catalogId);
        if (!byId1.isPresent()) {
            return CommonResult.failed("意料之外的错误！");
        }
        DscCatalog dscCatalog = byId1.get();
        dscCatalog.getChildren().add(catalogChildrenDTO);
        dscCatalog.setTotal(dscCatalog.getTotal() + 1);
        dscCatalog.setUpdatedTime(DateUtil.format(new Date(), "yyyy-MMMM-dddd HH:mm:ss"));
        dscCatalogDAO.save(dscCatalog);
        return CommonResult.success("导入个人空间成功！");
    }

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
        String fileRoot = System.getProperty("os.name").startsWith("Windows") ? fileRootPathWin : fileRootPath;
        String separator = File.separator;
        String fullPath = fileRoot + bucket + separator + objectKey;
        String unzipDirPath = System.getProperty("os.name").startsWith("Windows") ? MessageFormat.format(unzipTempPathWin, UUID.randomUUID()) : MessageFormat.format(unzipTempPath, UUID.randomUUID());
        File unzipDir = new File(unzipDirPath);
        unzipDir.mkdirs();  //创建临时解压文件夹
        int successCount = 0;
        List<JSONObject> fileObjects;
        try {
            fileObjects = FileUtils.zipUncompress(fullPath, unzipDirPath);
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
                TaskInfoDTO data = sysUploadTaskService.initTask(initTaskParam);
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
                CommonResult<String> upload = this.create(uploadFileDTO);
                if (upload.getCode() == 200) {
                    successCount++;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return CommonResult.failed("解压失败！");
        }
        //删除临时解压文件夹
        FileUtils.deleteDirectory(unzipDirPath);
        if (successCount >= fileObjects.size()) {
            return CommonResult.success("解压全部完成！");
        } else {
            return CommonResult.success("解压部分完成！");
        }
    }

}
