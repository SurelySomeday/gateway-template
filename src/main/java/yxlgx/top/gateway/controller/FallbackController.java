package yxlgx.top.gateway.controller;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR;

import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import yxlgx.top.gateway.base.constants.Constants;
import yxlgx.top.gateway.base.exception.BaseException;
import yxlgx.top.gateway.base.util.GatewayMsgEnum;

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
        serverHttpResponse.getHeaders().set(Constants.RESPONSE_HEADER_CODE_NAME, GatewayMsgEnum.SERVICE_NOT_AVAILABLE.getCode());
        serverHttpResponse.getHeaders().set(Constants.RESPONSE_HEADER_MESSAGE_NAME, GatewayMsgEnum.SERVICE_NOT_AVAILABLE.getEncodeMsg());

        return Mono.empty();
    }
}
