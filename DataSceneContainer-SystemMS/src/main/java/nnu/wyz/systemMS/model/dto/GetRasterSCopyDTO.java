package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/7/30 18:36
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetRasterSCopyDTO {

    private String userId;

    private String sceneId;

    private String rasterSId;
}
