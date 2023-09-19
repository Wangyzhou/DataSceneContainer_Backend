//package nnu.wyz.Filter;
//
//import cn.hutool.core.collection.ListUtil;
//import nnu.wyz.Config.IgnoredUrls;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.core.Ordered;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//import java.net.URI;
//import java.util.List;
//import java.util.Objects;
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
//    Logger logger = LoggerFactory.getLogger(TokenFilter.class);
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//        logger.info(exchange.getRequest().getRemoteAddress() + "访问资源: " + exchange.getRequest().getURI());
//        return chain.filter(exchange);
//    }
//
//    @Override
//    public int getOrder() {
//        return -99;
//    }
//}
