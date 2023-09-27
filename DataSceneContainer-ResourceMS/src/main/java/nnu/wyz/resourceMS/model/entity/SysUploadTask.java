package nnu.wyz.resourceMS.model.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SysUploadTask implements Serializable {

    @Id
    private String id;
    //分片上传的uploadId
    private String uploadId;
    //文件唯一标识（md5）
    private String fileIdentifier;
    //文件名
    private String fileName;
    //所属桶名
    private String bucketName;
    //文件的key
    private String objectKey;
    //文件大小（byte）
    private Long totalSize;
    //每个分片大小（byte）
    private Long chunkSize;
    //分片数量
    private Integer chunkNum;

}
