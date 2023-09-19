package nnu.wyz.userMS.entity;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author wyz
 * @since 2023-08-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(value="DscUser对象", description="")
public class DscUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String userName;

    private String password;

    private String email;

    private String institution;

    private String registerDate;

    private Byte[] avatar;

    private int enabled;

    private String activeCode;

}
