package nnu.wyz.systemMS.model.entity;

import lombok.Data;

import java.util.List;

/**
 * @author tjk
 * @date 2025/1/3
 * @Description
 */
@Data
public class DscModelParams {

    private List<DscModelInnerParams> inputs;

    private List<DscModelInnerParams> outputs;

    private List<DscModelInnerParams> options;
}
