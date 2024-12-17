package nnu.wyz.systemMS.model.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import nnu.wyz.systemMS.model.dto.DscCodeFileDTO;
import org.springframework.data.annotation.Id;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class DscCodeFile {
    @Id
    private String id;
    private String fileName;  // 存储文件名
    private String code;      // 存储文件内容
    private String description;
    private String createUser;
    private String createdUserID;
    private String updateDate;
    // 构造器
    public DscCodeFile(DscCodeFileDTO dscCodeFileDTO) {
        this.fileName = dscCodeFileDTO.getFileName();
        this.code = dscCodeFileDTO.getCode();
        this.description = dscCodeFileDTO.getDescription();
        this.createUser = dscCodeFileDTO.getCreateUser();
        this.createdUserID = dscCodeFileDTO.getCreatedUserID();
    }
}
