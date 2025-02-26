package nnu.wyz.systemMS.model.entity.codeModel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParamsConstrains {
    private String[] choices;
    private String defaultValue;
    private String min;
    private String max;
}
