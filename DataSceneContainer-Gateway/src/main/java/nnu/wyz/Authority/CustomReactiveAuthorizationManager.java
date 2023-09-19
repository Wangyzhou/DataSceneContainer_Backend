package nnu.wyz.Authority;

import nnu.wyz.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/18 15:52
 */

@Component
public class CustomReactiveAuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
    @Autowired
    private RedisCache redisCache;

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, AuthorizationContext object) {
        ServerHttpRequest request = object.getExchange().getRequest();
        return authentication.map(auth -> {
            Object principal = auth.getPrincipal();
            String username;
            String jti = "";
            if (principal instanceof Jwt) {
                username = ((Jwt) principal).getClaimAsString("user_name");
                jti = ((Jwt) principal).getClaimAsString("jti");
            } else {
                username = principal.toString();
            }
            //黑名单Token拒绝访问
            Object userId = redisCache.getCacheObject(jti);
            if(!Objects.isNull(userId)) {
                return new AuthorizationDecision(false);
            }
//            boolean hasPerssion = false;
//            Map<String, Object> claims = ((Jwt) principal).getClaims();
//            List<String> authorities = castList(claims.get("authorities"), String.class);
//            if (StringUtils.hasText(username) && !"anonymousUser".equals(username)) {
//                for (String uri : authorities) {
//                    //验证用户拥有的资源权限是否与请求的资源相匹配
//                    if (new AntPathMatcher().match(uri, request.getURI().getPath())) {
//                        hasPerssion = true;
//                        break;
//                    }
//                }
//            }
            return new AuthorizationDecision(true);
        });
    }

    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<T>();
        if (obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return null;
    }
}
