package nnu.wyz.systemMS.service.iml;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import nnu.wyz.domain.CommonResult;
import nnu.wyz.domain.ResultCode;
import nnu.wyz.systemMS.config.MinioConfig;
import nnu.wyz.systemMS.config.MongoTransactional;
import nnu.wyz.systemMS.dao.DscCatalogDAO;
import nnu.wyz.systemMS.dao.DscFileDAO;
import nnu.wyz.systemMS.dao.DscUserDAO;
import nnu.wyz.systemMS.dao.SysUploadTaskDAO;
import nnu.wyz.systemMS.model.dto.*;
import nnu.wyz.systemMS.model.entity.*;
import nnu.wyz.systemMS.server.WebSocketServer;
import nnu.wyz.systemMS.service.DscFileService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/29 16:24
 */
@Service
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
    private MinioConfig minioConfig;

    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 文件上传，文件可以重复上传，但同一目录下不能有同名文件
     * 当前目录不能有重名文件，整个用户空间不能有两份相同资源
     *
     * @param uploadFileDTO
     * @return
     */
    @MongoTransactional
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
        String fileId;
        if ((fileId = task.getFileId()) != null) {
            return CommonResult.failed("用户空间存在相同文件资源" + fileId + "，请勿重复上传！");
        }
        Optional<DscCatalog> byId = dscCatalogDAO.findById(catalogId);
        if (!byId.isPresent()) {
            return CommonResult.failed("未找到载体目录!");
        }
        DscCatalog dscCatalog = byId.get();
        List<CatalogChildrenDTO> children = dscCatalog.getChildren();
        //2、判断 当前目录有无同名文件，有则上传失败，删除minio中已上传的文件，删除s3任务信息，返回失败信息
        for (CatalogChildrenDTO next : children) {
            //孩子节点不为folder且文件名出现冲突
            if (!next.getType().equals("folder") && next.getName().equals(task.getFileName())) {
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(task.getBucketName(), task.getObjectKey());
                amazonS3.deleteObject(deleteObjectRequest);
                sysUploadTaskDAO.delete(task);
                return CommonResult.failed(ResultCode.VALIDATE_FAILED, "在该目录下存在同名文件，上传失败！");
            }
        }
        //3、利用S3文件接口获取文件信息，入库、父目录更新、完成上传
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
                .setIsEnablePublish(ext.equals("shp"))
                .setCreatedUser(userId)
                .setSize(size)
                .setCreatedTime(dateTime)
                .setUpdatedTime(dateTime)
                .setPreviewCount(0L)
                .setDownloadCount(0L)
                .setOwnerCount(1L)
                .setPublishCount(0L)
                .setBucketName(task.getBucketName())
                .setObjectKey(task.getObjectKey())
                .setPerms(31);  //默认文件分享时具有全部的权限，也就是完全分享
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
//    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class, timeout = 120)
    @Override
//    @MongoTransactional
    public CommonResult<String> delete(DeleteFileDTO deleteFileDTO) {
        String fileId = deleteFileDTO.getFileId();
        String userId = deleteFileDTO.getUserId();
        String catalogId = deleteFileDTO.getCatalogId();
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed("文件不存在！");
        }
        DscFileInfo dscFileInfo = byId.get();
        dscFileInfo.setOwnerCount(dscFileInfo.getOwnerCount() - 1);     //文件拥有者数 - 1
        dscFileDAO.save(dscFileInfo);

        //进行目录孩子节点的摘除
        DscCatalog dscCatalog = dscCatalogDAO.findDscCatalogByIdAndUserId(catalogId, userId);
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
        dscCatalog.setUpdatedTime(DateUtil.format(new Date(), "yyyy-MMMM-dddd HH:mm:ss"));
        dscCatalogDAO.save(dscCatalog);

        if (dscFileInfo.getOwnerCount() < 1) {   //未有人拥有该资源，删除任务、删除源文件、删除文件记录
            SysUploadTask task = sysUploadTaskDAO.findSysUploadTaskByFileId(fileId);
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(minioConfig.getBucketName(), dscFileInfo.getObjectKey());
            amazonS3.deleteObject(deleteObjectRequest);
            if (!Objects.isNull(task)) {
                sysUploadTaskDAO.delete(task);
            }
            dscFileDAO.delete(dscFileInfo);
        }
        return CommonResult.success("删除成功!");
    }

    /**
     * 文件下载
     *
     * @param response
     */
    @MongoTransactional
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
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, objectKey);
        S3Object s3Object = amazonS3.getObject(getObjectRequest);
        InputStream delegateStream = s3Object.getObjectContent().getDelegateStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(delegateStream);
        OutputStream out;
        try {
            response.setContentType(s3Object.getObjectMetadata().getContentType());
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
    public CommonResult<JSONObject> getFileInfo(String userId, String fileId) {
        Optional<DscFileInfo> byId = dscFileDAO.findById(fileId);
        if (!byId.isPresent()) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "未找到此文件！");
        }
        DscFileInfo dscFileInfo = byId.get();
        HashMap<String, Object> fileInfoMap = new HashMap<>();
        fileInfoMap.put("id", dscFileInfo.getId());
        fileInfoMap.put("name", dscFileInfo.getFileName());
        fileInfoMap.put("type", dscFileInfo.getFileSuffix());
        fileInfoMap.put("isEnablePublish", dscFileInfo.getIsEnablePublish());
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
    @MongoTransactional
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
        if(!byId1.isPresent()) {
            return CommonResult.failed("意料之外的错误！");
        }
        DscCatalog dscCatalog = byId1.get();
        dscCatalog.getChildren().add(catalogChildrenDTO);
        dscCatalog.setTotal(dscCatalog.getTotal() + 1);
        dscCatalog.setUpdatedTime(DateUtil.format(new Date(), "yyyy-MMMM-dddd HH:mm:ss"));
        dscCatalogDAO.save(dscCatalog);
        return CommonResult.success("导入个人空间成功！");
    }
}
