package nnu.wyz.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/16 10:13
 */

@Configuration
public class TokenConfig {

//    private String SIGNING_KEY = "opengms:dsc:2!q@3$6&";

    //使用JwtToken
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    //JwtToken 产生策略
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//        converter.setSigningKey(SIGNING_KEY); //对称秘钥，资源服务器使用该秘钥来验证
        converter.setKeyPair(keyPair());  //非对称加密
        return converter;
    }
    @Bean
    public KeyPair keyPair() {
        KeyStoreKeyFactory factory = new KeyStoreKeyFactory(
                new ClassPathResource("ninja.jks"), "ninja980903".toCharArray());
        return factory.getKeyPair(
                "ninja-key", "ninja980903".toCharArray());
    }
}
