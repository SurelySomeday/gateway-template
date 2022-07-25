package yxlgx.top.gateway.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.tagsprovider.GatewayTagsProvider;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import yxlgx.top.gateway.base.constants.Constants;
import yxlgx.top.gateway.base.util.FilterUtil;
import yxlgx.top.gateway.domain.LogPushInfo;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * 过滤器执行顺序不对，暂未解决
 *
 * @author yx
 * @date 2022/01/26
 * @description
 **/
@Slf4j
@Component
public class LogFilter implements GlobalFilter, Ordered {


    private final MeterRegistry meterRegistry;

    private GatewayTagsProvider compositeTagsProvider;

    public LogFilter(MeterRegistry meterRegistry, List<GatewayTagsProvider> tagsProviders) {
        this.meterRegistry = meterRegistry;
        this.compositeTagsProvider = tagsProviders.stream().reduce(exchange -> Tags.empty(), GatewayTagsProvider::and);
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Timer.Sample sample = Timer.start(meterRegistry);
        return chain.filter(exchange).doOnSuccess(aVoid -> {
            endTimerRespectingCommit(exchange,sample);
                })
                .doOnError(throwable -> {
                    endTimerRespectingCommit(exchange,sample);
                });
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    private void endTimerRespectingCommit(ServerWebExchange exchange,Timer.Sample sample) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            sendLog(exchange,sample);
        } else {
            response.beforeCommit(() -> {
                sendLog(exchange,sample);
                return Mono.empty();
            });
        }

    }



    private void sendLog(ServerWebExchange exchange,Timer.Sample sample) {
        Tags tags = compositeTagsProvider.apply(exchange);


        long nanoSeconds = sample.stop(meterRegistry.timer( "requests", tags));
        Duration duration = Duration.ofNanos(nanoSeconds);
        //log.info("cos:"+duration.toMillis()+" requests tags: " + tags);
        try {
            //日志对象
            LogPushInfo logPushInfo = FilterUtil.generateLog(exchange);
            logPushInfo.setCallDuration(BigDecimal.valueOf(duration.toMillis()));
            //根据contentType判断需要拦截处理的数据,一般可以拦截json和text/plain等类型,若是字节数据可能会有异常
            //下面代码也可以修改响应头或者body
            if (FilterUtil.isTargetLogMediaType(exchange.getResponse())) {
                Object body = exchange.getAttributes().getOrDefault(Constants.CACHED_RESPONSE_BODY, "");
                Object length = exchange.getAttributes().getOrDefault(Constants.CACHED_RESPONSE_BODY_LENGTH, 0);
                exchange.getAttributes().remove(Constants.CACHED_RESPONSE_BODY);
                exchange.getAttributes().remove(Constants.CACHED_RESPONSE_BODY_LENGTH);
                logPushInfo.setResponseContent(body.toString());
                logPushInfo.setResponseLength(Long.parseLong(length.toString()));
            }
            HttpHeaders headers = exchange.getResponse().getHeaders();
            if (headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
                String first = headers.getFirst(HttpHeaders.CONTENT_LENGTH);
                if (StringUtils.isNotBlank(first)) {
                    logPushInfo.setResponseLength(Long.parseLong(first));
                }
            }
            log.info(logPushInfo.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
