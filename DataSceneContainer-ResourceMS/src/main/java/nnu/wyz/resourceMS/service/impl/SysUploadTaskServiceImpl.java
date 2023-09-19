package nnu.wyz.resourceMS.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import nnu.wyz.resourceMS.config.MinioConfig;
import nnu.wyz.resourceMS.constant.MinioConstant;
import nnu.wyz.resourceMS.dao.SysUploadTaskDAO;
import nnu.wyz.resourceMS.model.dto.TaskInfoDTO;
import nnu.wyz.resourceMS.model.dto.TaskRecordDTO;
import nnu.wyz.resourceMS.model.entity.SysUploadTask;
import nnu.wyz.resourceMS.model.param.InitTaskParam;
import nnu.wyz.resourceMS.service.SysUploadTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分片上传-分片任务记录(SysUploadTask)表服务实现类
 *
 * @since 2022-08-22 17:47:31
 */
@Service("sysUploadTaskService")
public class SysUploadTaskServiceImpl implements SysUploadTaskService {

    @Resource
    private AmazonS3 amazonS3;

    @Resource
    private MinioConfig minioProperties;


    @Autowired
    private SysUploadTaskDAO sysUploadTaskDAO;

    @Override
    public SysUploadTask getByIdentifier(String identifier) {
        return  sysUploadTaskDAO.findSysUploadTaskByFileIdentifier(identifier);
    }


    @Override
    public TaskInfoDTO initTask(InitTaskParam param) {

        Date currentDate = new Date();
        String bucketName = minioProperties.getBucketName();
        String fileName = param.getFileName();
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1, fileName.length());
        String key = StrUtil.format("{}/{}.{}", DateUtil.format(currentDate, "YYYY-MM-dd"), IdUtil.randomUUID(), suffix);
        String contentType = MediaTypeFactory.getMediaType(key).orElse(MediaType.APPLICATION_OCTET_STREAM).toString();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        InitiateMultipartUploadResult initiateMultipartUploadResult = amazonS3
                .initiateMultipartUpload(new InitiateMultipartUploadRequest(bucketName, key).withObjectMetadata(objectMetadata));
        String uploadId = initiateMultipartUploadResult.getUploadId();

        SysUploadTask task = new SysUploadTask();
        int chunkNum = (int) Math.ceil(param.getTotalSize() * 1.0 / param.getChunkSize());
        task
                .setId(IdUtil.objectId())
                .setBucketName(minioProperties.getBucketName())
                .setChunkNum(chunkNum)
                .setChunkSize(param.getChunkSize())
                .setTotalSize(param.getTotalSize())
                .setFileIdentifier(param.getIdentifier())
                .setFileName(fileName)
                .setObjectKey(key)
                .setUploadId(uploadId);
        sysUploadTaskDAO.insert(task);
        return new TaskInfoDTO().setFinished(false).setTaskRecord(TaskRecordDTO.convertFromEntity(task)).setPath(getPath(bucketName, key));
    }

    @Override
    public String getPath(String bucket, String objectKey) {
        return StrUtil.format("{}/{}/{}", minioProperties.getEndpoint(), bucket, objectKey);
    }

    @Override
    public TaskInfoDTO getTaskInfo(String identifier) {
        SysUploadTask task = getByIdentifier(identifier);
        if (task == null) {
            return null;
        }
        TaskInfoDTO result = new TaskInfoDTO().setFinished(true).setTaskRecord(TaskRecordDTO.convertFromEntity(task)).setPath(getPath(task.getBucketName(), task.getObjectKey()));

        boolean doesObjectExist = amazonS3.doesObjectExist(task.getBucketName(), task.getObjectKey());
        if (!doesObjectExist) {
            // 未上传完，返回已上传的分片
            ListPartsRequest listPartsRequest = new ListPartsRequest(task.getBucketName(), task.getObjectKey(), task.getUploadId());
            PartListing partListing = amazonS3.listParts(listPartsRequest);
            result.setFinished(false).getTaskRecord().setExitPartList(partListing.getParts());
        }
        return result;
    }

    @Override
    public String genPreSignUploadUrl(String bucket, String objectKey, Map<String, String> params) {
        Date currentDate = new Date();
        Date expireDate = DateUtil.offsetMillisecond(currentDate, MinioConstant.PRE_SIGN_URL_EXPIRE.intValue());
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, objectKey)
                .withExpiration(expireDate).withMethod(HttpMethod.PUT);
        if (params != null) {
            params.forEach((key, val) -> request.addRequestParameter(key, val));
        }
        URL preSignedUrl = amazonS3.generatePresignedUrl(request);
        return preSignedUrl.toString();
    }

    @Override
    public void merge(String identifier) {
        SysUploadTask task = getByIdentifier(identifier);
        if (task == null) {
            throw new RuntimeException("分片任务不存在");
        }

        ListPartsRequest listPartsRequest = new ListPartsRequest(task.getBucketName(), task.getObjectKey(), task.getUploadId());
        PartListing partListing = amazonS3.listParts(listPartsRequest);
        List<PartSummary> parts = partListing.getParts();
        if (!task.getChunkNum().equals(parts.size())) {
            // 已上传分块数量与记录中的数量不对应，不能合并分块
            throw new RuntimeException("分片缺失，请重新上传");
        }
        CompleteMultipartUploadRequest completeMultipartUploadRequest = new CompleteMultipartUploadRequest()
                .withUploadId(task.getUploadId())
                .withKey(task.getObjectKey())
                .withBucketName(task.getBucketName())
                .withPartETags(parts.stream().map(partSummary -> new PartETag(partSummary.getPartNumber(), partSummary.getETag())).collect(Collectors.toList()));
        amazonS3.completeMultipartUpload(completeMultipartUploadRequest);
    }

    @Override
    public void downlod(String md5, HttpServletResponse response) {
        SysUploadTask t = getByIdentifier(md5);
        String objectKey = t.getObjectKey();
        String bucketName = minioProperties.getBucketName();
        GetObjectRequest objectRequest = new GetObjectRequest(bucketName, objectKey);
        S3Object object = amazonS3.getObject(objectRequest);
        System.out.println("object.getObjectMetadata() = " + object.getObjectMetadata());
        InputStream delegateStream = object.getObjectContent().getDelegateStream();
        BufferedInputStream bufferedInputStream = new BufferedInputStream(delegateStream);
        OutputStream out;
        try {
            response.setContentType("application/force-download");
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(t.getFileName(), "UTF-8"));
            out = response.getOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = bufferedInputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            response.flushBuffer();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
