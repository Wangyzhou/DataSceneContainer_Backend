package nnu.wyz.systemMS.model.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class DscCodeFileDTO {

    private String fileName;  // 存储文件名
    private String code;      // 存储文件内容
    private String description;
    private String createUser;
    private String createdUserID;

}

