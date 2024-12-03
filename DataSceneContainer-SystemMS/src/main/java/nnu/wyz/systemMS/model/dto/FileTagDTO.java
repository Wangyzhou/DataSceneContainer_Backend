package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @Desription：
 * @Author：mfz
 * @Date：2024/11/18 20:28
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class FileTagDTO {

    private String fileId;
    private String fileName;
    private String type;
    private List<String> location;
    private List<String> attr;
    private List<String> other;
}
