package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/2/23 10:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenderTifDTO {

    private String userId;

    private String rasterSId;

    private int band;

    private String colorMap;
}
