package nnu.wyz.resourceMS.service;

import nnu.wyz.resourceMS.model.dto.TaskInfoDTO;
import nnu.wyz.resourceMS.model.entity.SysUploadTask;
import nnu.wyz.resourceMS.model.param.InitTaskParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 分片上传-分片任务记录(SysUploadTask)表服务接口
 *
 * @since 2022-08-22 17:47:30
 */
public interface SysUploadTaskService {

    /**
     * 根据md5标识获取分片上传任务
     *
     * @param identifier
     * @return
     */
    SysUploadTask getByIdentifier(String identifier);

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
     * @param identifier
     * @return
     */
    TaskInfoDTO getTaskInfo(String identifier);

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
    void merge(String identifier);

    void downlod(String md5, HttpServletResponse response);
}
