package nnu.wyz.systemMS.model.entity.codeModel.GeoActModelParams;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoActParamsConstrains {
    private List<GeoActChoice> choices;
    private String defaultValue;
    private String min;
    private String max;
}
