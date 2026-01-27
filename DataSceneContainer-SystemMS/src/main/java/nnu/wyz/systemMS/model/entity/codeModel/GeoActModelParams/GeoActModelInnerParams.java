package nnu.wyz.systemMS.model.entity.codeModel.GeoActModelParams;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoActModelInnerParams {
    private String name;
    private String identifier;
    private String category;
    private String type;
    @JsonProperty("isOptional")
    private boolean isOptional;
    private GeoActParamsConstrains constraints;
}
