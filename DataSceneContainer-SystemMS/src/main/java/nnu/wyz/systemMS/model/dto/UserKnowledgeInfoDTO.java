package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/20 16:18
 */

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserKnowledgeInfoDTO {

    private String datasetId;

    private String documentId;
}
