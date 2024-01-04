package nnu.wyz.systemMS.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/12/13 10:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DscToolRawParams {

    private String name;

    private String value;

    private String extra;   //输出文件时，要携带输出文件的目录ID

}
