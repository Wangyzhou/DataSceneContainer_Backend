package nnu.wyz.systemMS.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/20 19:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageableDTO {
    private String criteria;
    private String keyword;
    private Integer pageIndex;
    private Integer pageSize;
}
