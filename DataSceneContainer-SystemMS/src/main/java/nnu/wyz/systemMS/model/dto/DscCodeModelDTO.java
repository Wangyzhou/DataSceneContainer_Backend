package nnu.wyz.systemMS.model.dto;
import lombok.*;
import nnu.wyz.systemMS.model.entity.codeModel.DscCodeModelParams;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DscCodeModelDTO {
    private String id;
    private String name;
    private String category;
    private String script;
    private String description;
    private String isEnabled;
    private DscCodeModelParams params;
    private String creater;
    private String createrId;
    private String createDate;
    private List<String> reference;
}
