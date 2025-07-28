package nnu.wyz.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDTO {
    private String clientId;
    private String refreshToken;
    private String clientSecret;
    private String grantType;
}
