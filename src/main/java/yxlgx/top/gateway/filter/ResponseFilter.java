package yxlgx.top.gateway.filter;

import java.util.Objects;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import yxlgx.top.gateway.base.constants.Constants;
import yxlgx.top.gateway.base.properties.ProjectGatewayProperties;
import yxlgx.top.gateway.base.util.GatewayMsgEnum;
import yxlgx.top.gateway.filter.custom.CustomModifyResponseBodyGatewayFilterFactory;
import yxlgx.top.gateway.filter.response.ResponseBodyRewriteFunction;

import javax.annotation.Resource;

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

    @Resource
    ProjectGatewayProperties projectGatewayProperties;
    private final GatewayFilter delegate;

    public ResponseFilter(CustomModifyResponseBodyGatewayFilterFactory customModifyResponseBodyGatewayFilterFactory,
                          ResponseBodyRewriteFunction rewriteFunction) {
        delegate = customModifyResponseBodyGatewayFilterFactory
                .apply(new CustomModifyResponseBodyGatewayFilterFactory.Config()
                        .setRewriteFunction(rewriteFunction)
                        .setInClass(byte[].class)
                        .setOutClass(byte[].class));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //配置调用成功的请求头
        if (!(exchange.getResponse().getHeaders().containsKey(Constants.RESPONSE_HEADER_CODE_NAME) &&
                Objects.equals(exchange.getResponse().getHeaders().getFirst(Constants.RESPONSE_HEADER_CODE_NAME), GatewayMsgEnum.SYSTEM_ERROR.getCode()))) {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            headers.set(Constants.RESPONSE_HEADER_CODE_NAME ,GatewayMsgEnum.SUCCESS.getCode());
            headers.set(Constants.RESPONSE_HEADER_MESSAGE_NAME, GatewayMsgEnum.SUCCESS.getMsg());
        }
        //拦截responseBody，文件流较大的情况下可能出现问题
        if(projectGatewayProperties.isLogResponse()) {
            return delegate.filter(exchange, chain);
        }else {
            // 不拦截，直接return
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

}
