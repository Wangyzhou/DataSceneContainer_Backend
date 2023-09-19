package nnu.wyz.systemMS.model.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;
import nnu.wyz.systemMS.model.dto.CatalogChildrenDTO;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.List;

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

    private List<CatalogChildrenDTO> children;

    private String parent;

    private String createdTime;

    private String updatedTime;

}
