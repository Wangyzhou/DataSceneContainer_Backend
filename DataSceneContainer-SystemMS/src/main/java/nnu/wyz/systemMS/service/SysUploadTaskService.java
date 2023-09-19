package nnu.wyz.systemMS.service;


import nnu.wyz.systemMS.model.dto.TaskInfoDTO;
import nnu.wyz.systemMS.model.entity.SysUploadTask;
import nnu.wyz.systemMS.model.param.InitTaskParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 分片上传-分片任务记录(SysUploadTask)表服务接口
 *
 * @since 2022-08-22 17:47:30
 */
public interface SysUploadTaskService {


    /**
     * 根据user、md5标识获取分片上传任务
     *
     * @param uploader
     * @param md5
     * @return
     */
    SysUploadTask getByUploaderAndMd5(String uploader, String md5);

    /**
     * 初始化一个任务
     */
    TaskInfoDTO initTask(InitTaskParam param);

    /**
     * 获取文件地址
     *
     * @param bucket
     * @param objectKey
     * @return
     */
    String getPath(String bucket, String objectKey);

    /**
     * 获取上传进度
     *
     * @param userId
     * @param identifier
     * @return
     */
    TaskInfoDTO getTaskInfo(String userId, String identifier);

    /**
     * 生成预签名上传url
     *
     * @param bucket    桶名
     * @param objectKey 对象的key
     * @param params    额外的参数
     * @return
     */
    String genPreSignUploadUrl(String bucket, String objectKey, Map<String, String> params);

    /**
     * 合并分片
     *
     * @param identifier
     */
    void merge(String userId, String identifier);

}
