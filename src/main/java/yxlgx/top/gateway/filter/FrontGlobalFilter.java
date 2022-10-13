package yxlgx.top.gateway.filter;

import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import yxlgx.top.gateway.base.constants.Constants;

import javax.annotation.Resource;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * @author yx
 * @date 2022/01/20
 * @description 前置过滤器，优先级最高，用于处理请求开始前的信息。
 **/
@Component
public class FrontGlobalFilter implements GatewayFilter, GlobalFilter, Ordered {

    @Resource
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(Constants.START_TIME, System.currentTimeMillis());
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR); // 获取路由。
        //通知缓存请求body
        if(route!=null) {
            applicationEventPublisher.publishEvent(new EnableBodyCachingEvent(this, route.getId()));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
