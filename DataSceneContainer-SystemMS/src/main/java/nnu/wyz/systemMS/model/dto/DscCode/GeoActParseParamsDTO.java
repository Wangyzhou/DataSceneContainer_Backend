package nnu.wyz.systemMS.model.dto.DscCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoActParseParamsDTO {
    private String id;
    private String option;
    private String baseIp;
    private String executor;
    private String accessToken;
    private String sceneCatalogId;
}
