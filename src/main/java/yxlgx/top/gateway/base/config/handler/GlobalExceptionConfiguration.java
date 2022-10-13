package yxlgx.top.gateway.base.config.handler;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import yxlgx.top.gateway.base.constants.Constants;
import yxlgx.top.gateway.base.exception.BaseException;
import yxlgx.top.gateway.base.util.FilterUtil;
import yxlgx.top.gateway.base.util.GatewayMsgEnum;
import yxlgx.top.gateway.domain.LogPushInfo;

/**
 * @author yx
 * @date 2021/12/18
 **/
@Slf4j
@Order(-1)
@Component
@RequiredArgsConstructor
public class GlobalExceptionConfiguration implements ErrorWebExceptionHandler {


    @SneakyThrows
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        //打印栈信息
        if(!(ex instanceof BaseException)){
            log.error(ExceptionUtils.getStackTrace(ex));
        }
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        //没有请求开始时间，说明不是需要代理的请求
        if(!exchange.getAttributes().containsKey(Constants.START_TIME)){
            return Mono.error(ex);
        }
        try {
            LogPushInfo logPushInfo = FilterUtil.generateLog(exchange);
            logPushInfo.setError(true);
            logPushInfo.setErrorInfo(ExceptionUtils.getRootCauseMessage(ex));
            log.error(logPushInfo.toString());
        }catch (Exception e){
            log.error(ExceptionUtils.getStackTrace(e));
        }
        //自定义异常
        if (ex instanceof BaseException) {
            BaseException baseException = (BaseException) ex;
            // header set
            GatewayMsgEnum msg = baseException.getMsg();
            response.getHeaders().set(Constants.RESPONSE_HEADER_CODE_NAME, msg.getCode());
            response.getHeaders().set(Constants.RESPONSE_HEADER_MESSAGE_NAME,msg.getEncodeMsg());
            return response
                    .writeWith(Mono.fromSupplier(() -> {
                        DataBufferFactory bufferFactory = response.bufferFactory();
                        try {
                            return bufferFactory.wrap(new byte[0]);
                        } catch (Exception e) {
                            log.warn("Error writing response", ex);
                            return bufferFactory.wrap(new byte[0]);
                        }
                    }));
            //未知异常
        } else {
            // header set
            response.getHeaders().set(Constants.RESPONSE_HEADER_CODE_NAME, GatewayMsgEnum.SYSTEM_ERROR.getCode());
            response.getHeaders().set(Constants.RESPONSE_HEADER_MESSAGE_NAME,GatewayMsgEnum.SYSTEM_ERROR.getEncodeMsg());
            ex.printStackTrace();
            return response
                    .writeWith(Mono.fromSupplier(() -> {
                        DataBufferFactory bufferFactory = response.bufferFactory();
                        try {
                            return bufferFactory.wrap(new byte[0]);
                        } catch (Exception e) {
                            log.warn("Error writing response", ex);
                            return bufferFactory.wrap(new byte[0]);
                        }
                    }));
        }


    }

}
