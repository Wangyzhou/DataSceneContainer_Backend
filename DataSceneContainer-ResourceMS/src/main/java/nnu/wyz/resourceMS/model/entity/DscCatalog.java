package nnu.wyz.resourceMS.model.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

/**
 * <p>
 * 
 * </p>
 *
 * @author yzwang
 * @since 2023-08-25
 */
@Data
@Accessors(chain = true)
@ApiModel(value="DscCatalog对象", description="")
public class DscCatalog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String userId;

    private String name;

    private Integer total;

    private Integer level;

    private String parent;


}
