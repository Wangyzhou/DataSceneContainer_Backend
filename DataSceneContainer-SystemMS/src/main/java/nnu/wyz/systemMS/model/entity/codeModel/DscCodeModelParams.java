package nnu.wyz.systemMS.model.entity.codeModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DscCodeModelParams {
    private List<DscCodeModelInnerParams> inputParams;
    private List<DscCodeModelInnerParams> optionParams;
    private List<DscCodeModelInnerParams> outputParams;
}
