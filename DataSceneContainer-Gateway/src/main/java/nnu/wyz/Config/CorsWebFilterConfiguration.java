package nnu.wyz.Config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @description:
 * @author: yzwang
 * @time: 2023/8/23 9:54
 */
@Component
@NoArgsConstructor
@Setter
@Getter
public class CorsWebFilterConfiguration {
    @Bean
    CorsWebFilter corsFilter() {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        //允许全部域名
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        ArrayList<String> Methods = new ArrayList<>();
        Methods.add("POST");
        Methods.add("PUT");
        Methods.add("GET");
        Methods.add("DELETE");
        Methods.add("OPTIONS");
        config.setAllowedMethods(Methods);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
