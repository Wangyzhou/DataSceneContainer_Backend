package nnu.wyz.systemMS.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2025/5/26 17:13
 */
@Data
public class MapPublishDTO {

    String mapName;

    String sceneId;

    String userId;

    JsonNode mapStyle;

    String introduction;
}
