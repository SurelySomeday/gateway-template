package yxlgx.top.gateway.filter;

import yxlgx.top.gateway.base.util.GatewayMsgEnum;
import yxlgx.top.gateway.filter.response.ResponseBodyRewriteFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Consumer;

/**
 * 响应处理
 *
 * @author yx
 * @date 2022/01/26
 * @description
 **/
@Component
@Slf4j
public class ResponseFilter implements GlobalFilter, Ordered {

    private final GatewayFilter delegate;

    public ResponseFilter(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory,
                          ResponseBodyRewriteFunction rewriteFunction) {
        delegate = modifyResponseBodyGatewayFilterFactory
                .apply(new ModifyResponseBodyGatewayFilterFactory.Config()
                        .setRewriteFunction(rewriteFunction)
                        .setInClass(byte[].class)
                        .setOutClass(byte[].class));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //配置调用成功的请求头
        if (!(exchange.getResponse().getHeaders().containsKey("code") &&
                Objects.equals(exchange.getResponse().getHeaders().getFirst("code"), GatewayMsgEnum.E_20001.getCode()))) {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.set("code" ,"0");
            headers.set("message", "ok");
        }
        //拦截responseBody，文件流较大的情况下出现问题
        return delegate.filter(exchange,chain);

        // 不拦截，直接return
        //return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

}
