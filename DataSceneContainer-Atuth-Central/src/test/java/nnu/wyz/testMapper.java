package nnu.wyz;

import com.alibaba.fastjson.JSON;
import nnu.wyz.entity.DscUser;
import nnu.wyz.entity.DscUser_Role;
import nnu.wyz.mapper.DscUserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/16 11:10
 */
@SpringBootTest
public class testMapper {

    @Autowired
    DscUserMapper dscUserMapper;

    @Autowired
    PasswordEncoder bCryptPasswordEncoder;

    @Test
    void test1() {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        List<DscUser_Role> DscUser_Roles = dscUserMapper.findAuthoritiesByUserId("a270426e-fc90-4e19-932a-839d5274e9e9");
        DscUser_Roles.forEach(perms -> authorities.add(new SimpleGrantedAuthority(perms.getPerm_code())));
        System.out.println("authoritiesByUserId = " + authorities);
    }

    @Test
    void test2() {
//        String encoded = bCryptPasswordEncoder.encode("ninja123");
//        System.out.println("encoded = " + encoded);
//        boolean matches = bCryptPasswordEncoder.matches("ninja123", "$2a$10$uhaBziJIo4Lbsf.s94avwOtwBn7Hj/MJVXQcuIfVbfTJ0Am0FWJsy");
//        System.out.println("matches = " + matches);
        String encoded = bCryptPasswordEncoder.encode("123456");
        System.out.println("encoded = " + encoded);
    }

    @Test
    public void createJWT() {
        //基于私钥生成JWT
        //创建一个密钥工厂
        //私钥的位置  本模块中resources中的changgou.jks就是私钥，public.key就是公钥
        ClassPathResource classPathResource = new ClassPathResource("ninja.jks");
        //密钥库的密码
        String keyPass = "ninja980903";
        /**
         * 参数1 私钥的位置
         * 参数2 密钥库的密码
         */
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource, keyPass.toCharArray());
        //基于工厂获取私钥
        //密钥的别名
        String alias = "ninja-key";
        //密钥的密码
        String password = "ninja980903";
        /**
         * 参数1 密钥的别名
         * 参数2 密钥的密码
         */
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, password.toCharArray());
        //将当前的私钥转为RSA的私钥
        RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();
        //生成jwt
        Map<String, String> map = new HashMap<>();
        map.put("company", "heima");
        map.put("address", "beijing");

        /**
         * 参数1 当前的令牌的内容
         * 参数2 签名（用RSA的私钥来做签名）
         */
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(map), new RsaSigner(rsaPrivateKey));
        String jwtEncoded = jwt.getEncoded();
        System.out.println(jwtEncoded);
    }

    @Value("${jwt.publicKey}")
    String publicKey;

    @Test
    void parseJWT() {
//        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoiYmVpamluZyIsImNvbXBhbnkiOiJoZWltYSJ9.JpvJgbmmMnH6WnujaPNAdhON9_cL4zDSXbe_esIHxPb3OniJK969En9r0SCaLLb_vZ1xTLHKDUx8bEhCG2SCGKv3PLCSF_RfFtUiBr4Qo3txaJjdX0O4ffgsvpCwkAqfuFoenLxW9L0wuz1O9fObw9I75AEddoyae5Hj9G_g99fGjAFiQMvIqIHVctLums0MKAWXSxHGp39U_qcqaLohx-0wOXM9jdZB2j68UwDcJcIzVOQrKmzCYKHX__6NYUIRF2L5_fzBzFqrTLpnslRmqmpbdAbrKjkJXJFcfyBUs3dzEQnhyelBrMbPpGz7zPaYCuZNIum0TwoVi8M49sk0HQ";
        String jwt = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2OTIyNjE0MzUsInVzZXJfbmFtZSI6IntcImVtYWlsXCI6XCJuaW5qYUAxNjMuY29tXCIsXCJpZFwiOlwiYTI3MDQyNmUtZmM5MC00ZTE5LTkzMmEtODM5ZDUyNzRlOWU5XCIsXCJpbnN0aXR1dGlvblwiOlwiTkpOVVwiLFwicGFzc3dvcmRcIjpcIiQyYSQxMCR1aGFCemlKSW80TGJzZi5zOTRhdndPdHdCbjdIai9NSlZYUWN1SWZWYmZUSjBBbTBGV0pzeVwiLFwidXNlck5hbWVcIjpcIm5pbmphXCJ9IiwiYXV0aG9yaXRpZXMiOlsicDEiXSwianRpIjoiMGJhNWZhYWEtN2E2YS00NDgwLTg1MGMtYmYzMGRkN2E1YjI3IiwiY2xpZW50X2lkIjoidGVzdDEiLCJzY29wZSI6WyJ3cml0ZSJdfQ.RrK-5YQtq1d3qV9vhBH2LJxNUUpom64PZ5c2j-Tl7AThCcW2z1Lqeu8Vs_-AF6FTwxEL96KicEsxcrHvmcX5pUPYO4N3exMLMnz2MDfN-esVFMjcswoNv4A7nle0vnppbF0h6IVk_a2JUgyRra47hsCAGrNjeagONJlHSVQrcAB7HKYj1pWRxAEcu69b0u3dS8x7Fb6ttG75Ldy59ITKRTu6DTp5Sa-HYGIA86JbJNMHipXQunMZxQi-c0zKU0Ri1Cx-N89FeqhPWe3lTKPovrHy0AKV1mz0zn61Vp9A2USjShGT3M_LqOvpPOyZ8zL29uehdbbpIdcn97KeTZbLdQ";
//        String publicKey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiluRji4NLXRxoyQsYH2Y1jGSjx9U22rX6sVe7Kvsw2Og7Ndhbwu/BbGW34xqoB5LL+99fT70LT14TCvRxVn4MnUyguH0WMlMklGOosGxjkRaXxovJaDW8tm2SN05C3bXn+/SBXsD5LIqhSjdbJxA6aEUXu8qFqQjhe2vMzwup/0XHxtyfHTfY3azEaFTzlfem88acHFkr5HtiJrXj0SdxxNehf0ylr+vLYQUjnww8RtJ7voTZIrtQqbXJIlcJ1puZGJOE6AuHVZtpcZoeJYFjEHR9CucFgjtkEUiyilKee3k2qPxaTnBsPCXon1jWcKoP9MFTVsvG5B8ipFQRRxyHwIDAQAB-----END PUBLIC KEY-----";
        //解析和验签jwt，获取令牌
        Jwt token = JwtHelper.decodeAndVerify(jwt, new RsaVerifier(publicKey));
        //解析令牌，获取令牌中的载荷
        String claims = token.getClaims();
        System.out.println(claims);//打印结果为{"address":"beijing","company":"heima"}
    }
}
