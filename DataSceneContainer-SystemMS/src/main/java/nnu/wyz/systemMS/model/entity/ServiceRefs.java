package nnu.wyz.systemMS.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Desription：用于更改服务引用时存放待更改的矢量和栅格服务的ids
 * @Author：mfz
 * @Date：2024/8/1 21:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRefs {

    private List<String> vectorRefs;

    private List<String> rasterRefs;
}
