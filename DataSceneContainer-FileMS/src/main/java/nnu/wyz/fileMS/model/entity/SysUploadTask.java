package nnu.wyz.fileMS.model.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ApiModel(value = "文件上传任务")
public class SysUploadTask implements Serializable {

    @ApiModelProperty(value = "文件上传任务ID")
    @Id
    private String id;
    //分片上传的uploadId
    @ApiModelProperty(value = "文件上传ID")
    private String uploadId;

    @ApiModelProperty(value = "文件ID")
    private String fileId;

    //文件唯一标识（md5）
    @ApiModelProperty(value = "文件MD5")
    private String fileIdentifier;
    //文件名
    @ApiModelProperty(value = "文件名")
    private String fileName;
    //所属桶名
    @ApiModelProperty(value = "所属桶")
    private String bucketName;
    //文件的key
    @ApiModelProperty(value = "对象Key")
    private String objectKey;
    //文件大小（byte）
    @ApiModelProperty(value = "文件总大小")
    private Long totalSize;
    //每个分片大小（byte）
    @ApiModelProperty(value = "文件块大小")
    private Long chunkSize;
    //分片数量
    @ApiModelProperty(value = "文件块数")
    private Integer chunkNum;

    @ApiModelProperty(value = "上传用户")
    private String uploader;
}
