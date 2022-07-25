package yxlgx.top.gateway.controller;

import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import yxlgx.top.gateway.base.exception.BaseException;

import java.net.URLEncoder;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR;

/**
 * @author yx
 * @date 2021/12/24
 **/
@RestController
public class FallbackController {


    @GetMapping("/fallbackA")
    public Mono<Void> fallbackA(ServerWebExchange exchange, ServerHttpResponse serverHttpResponse) throws Throwable {
        //获取异常信息，如果是自定义异常，统一交给全局异常处理
        Throwable throwable = (Throwable) exchange.getAttributes().get(CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        if (throwable instanceof BaseException) throw throwable;
        serverHttpResponse.getHeaders().set("code", "-1");
        serverHttpResponse.getHeaders().set("message", URLEncoder.encode("服务不可用！"+throwable.getMessage(), "UTF-8"));
        return Mono.empty();
    }

    @GetMapping("/test")
    public Mono<Void> test() throws NacosException {
        //System.out.println(configService.getConfig("gateway-flow-rules","DEFAULT_GROUP",5000));
        return Mono.empty();
    }
}
