package yxlgx.top.gateway.filter.request;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * @author yx
 * @date 2021/12/28
 * @description 可重写请求体
 **/
public class RequestBodyRewriteFunction implements RewriteFunction<byte[], byte[]> {

    @Override
    public Publisher<byte[]> apply(ServerWebExchange exchange, byte[] bytes) {
        ServerHttpResponse response = exchange.getResponse();
        if (!ServerWebExchangeUtils.isAlreadyRouted(exchange)){
            //请求

        }else {
            //响应
        }

        return Mono.just(bytes);
    }
}
