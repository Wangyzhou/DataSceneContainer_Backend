//package nnu.wyz.Filter;
//
//import cn.hutool.core.collection.ListUtil;
//import nnu.wyz.Config.IgnoredUrls;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.util.List;
//
///**
// * @description:
// * @author: yzwang
// * @time: 2023/8/17 22:24
// */
//@Component
//public class TokenFilter implements GlobalFilter, Ordered {
//
//    @Autowired
//    private IgnoredUrls ignoredUrls;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        String path = exchange.getRequest().getURI().getPath();
//        System.out.println("path = " + path);
//        System.out.println("exchange = " + exchange.getRequest());
//        //1、白名单放行
//        List<String> urls = ignoredUrls.getUrls();
//        System.out.println("urls = " + urls);
//        if (urls.stream().anyMatch(url -> url.equals(path))) {
//            return chain.filter(exchange);
//        }
//        //2、拿出token
//
//        return chain.filter(exchange);
//    }
//
//    @Override
//    public int getOrder() {
//        return 0;
//    }
//}
