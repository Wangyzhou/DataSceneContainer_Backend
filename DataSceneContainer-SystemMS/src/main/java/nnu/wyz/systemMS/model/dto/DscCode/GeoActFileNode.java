package nnu.wyz.systemMS.model.dto.DscCode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoActFileNode {
    private String id;
    private String name;
    private String type;
    private List<GeoActFileNode> children;
}
