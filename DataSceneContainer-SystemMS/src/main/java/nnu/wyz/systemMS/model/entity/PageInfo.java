package nnu.wyz.systemMS.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/9/21 14:35
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo<T> {

    private List<T> body;

    private Integer count;

    private Integer pageNum;

}
