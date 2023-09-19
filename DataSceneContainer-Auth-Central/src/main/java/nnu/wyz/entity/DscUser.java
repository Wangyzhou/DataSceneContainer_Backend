package nnu.wyz.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;

/**
 * <p>
 * 
 * </p>
 *
 * @author wyz
 * @since 2023-08-16
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
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

    private byte[] avatar;

    private int enabled;

    private String activeCode;

}
