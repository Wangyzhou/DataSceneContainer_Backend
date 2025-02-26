package nnu.wyz.systemMS.model.entity.codeModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DscCodeModelInnerParams {
    private String label;
    private String category;
    private String type;
    private ParamsConstrains constraints;
}
