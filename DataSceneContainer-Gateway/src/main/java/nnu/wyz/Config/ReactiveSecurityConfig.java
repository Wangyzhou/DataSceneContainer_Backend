package nnu.wyz.Config;

import cn.hutool.core.util.ArrayUtil;
import nnu.wyz.Authority.CustomReactiveAuthorizationManager;
import nnu.wyz.Handler.CustomServerAccessDeniedHandler;
import nnu.wyz.Handler.CustomServerAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.server.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;


/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/18 10:55
 */

@Configuration
@EnableWebFluxSecurity
@RefreshScope
public class ReactiveSecurityConfig {

    @Autowired
    private IgnoredUrls ignoredUrls;

    @Autowired
    private CustomReactiveAuthorizationManager customReactiveAuthorizationManager;
    @Autowired
    private CustomServerAccessDeniedHandler customServerAccessDeniedHandler;
    @Autowired
    private CustomServerAuthenticationEntryPoint customServerAuthenticationEntryPoint;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .oauth2ResourceServer()
                .jwt().and()    //jwt认证
                // 认证成功后没有权限操作
                .accessDeniedHandler(customServerAccessDeniedHandler)
                // 还没有认证时发生认证异常，比如token过期，token不合法
                .authenticationEntryPoint(customServerAuthenticationEntryPoint)
                // 将一个字符串token转换成一个认证对象
                .bearerTokenConverter(new ServerBearerTokenAuthenticationConverter())
                .and()
                .authorizeExchange()
                //白名单放行
                .pathMatchers(ArrayUtil.toArray(ignoredUrls.getUrls(), String.class)).permitAll()
                //自定义权限管理器
                .anyExchange().access(customReactiveAuthorizationManager)
                .and()
                .csrf().disable()
                .cors() //开启跨域支持
        ;
        return http.build();
    }
}
