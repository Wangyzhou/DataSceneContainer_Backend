package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/2/29 17:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FalseColorCompositeDTO {

    private String userId;

    private String rasterSId;

    private List<Integer> bandList;
}
