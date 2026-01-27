package nnu.wyz.systemMS.model.entity.codeModel.GeoActModelParams;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoActModelParams {
    private List<GeoActModelInnerParams> inputs;
    private List<GeoActModelInnerParams> options;
    private List<GeoActModelInnerParams> outputs;
}
