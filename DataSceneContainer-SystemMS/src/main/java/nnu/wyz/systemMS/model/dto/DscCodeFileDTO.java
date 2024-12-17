package nnu.wyz.systemMS.model.dto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Setter
@Getter
public class DscCodeFileDTO {

    private String id;
    private String fileName;  // 存储文件名
    private String code;      // 存储文件内容
    private String description;
    private String create_User;

    // 构造器
    public DscCodeFileDTO() {}

    public DscCodeFileDTO(String fileName, String fileContent) {
        this.fileName = fileName;
        this.code = fileContent;
    }

}

