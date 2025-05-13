package nnu.wyz.systemMS.model.entity.codeModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DscCodeModelInnerParams {
    private String name;
    private String identifier;
    private String category;
    private String type;
    private boolean isOptional;
    private ParamsConstrains constraints;
}
