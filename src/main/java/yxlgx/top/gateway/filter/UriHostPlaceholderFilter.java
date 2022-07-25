package yxlgx.top.gateway.filter;

import yxlgx.top.gateway.base.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.EnableBodyCachingEvent;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * 动态路由Filter,在这里处理请求
 *
 * @Author: yx
 * @Date: 2021/10/14
 **/
//@Component
public final class UriHostPlaceholderFilter extends AbstractGatewayFilterFactory {

    public static final Logger log = LoggerFactory.getLogger(UriHostPlaceholderFilter.class);


    @Resource
    ReactiveRedisTemplate<String,Object> reactiveRedisTemplate;


    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            //放置请求开始时间，用来计算请求耗时
            exchange.getAttributes().put(Constants.START_TIME, System.currentTimeMillis());
            Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR); // 获取路由。

            URI newUri = null;
            newUri = UriComponentsBuilder.newInstance().host(exchange.getRequest().getURI().getHost())
                    .scheme(exchange.getRequest().getURI().getScheme()).build().toUri(); // 重构URI。
            //修改后续处理的路由地址
            Route newRoute = Route.async()
                    .id("a")
                    .uri(newUri)
                    .metadata("connect-timeout", 60*1000)
                    .order(route.getOrder())
                    .asyncPredicate(route.getPredicate())
                    .filters(route.getFilters())
                    .build(); // 重构路由。
            //通知缓存body,后续可根据 ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR 去获取body
            getPublisher().publishEvent(new EnableBodyCachingEvent(this, route.getId()));
            //放置服务信息
            //exchange.getAttributes().put(Constants.SERVICE_INFO, serviceInfo);
            //放置路由信息
            exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, newRoute);
            //放置限流信息
            //exchange.getAttributes().put(Constants.LIMIT_INFO, limitInfo);
            return chain.filter(exchange);
        };
    }


}