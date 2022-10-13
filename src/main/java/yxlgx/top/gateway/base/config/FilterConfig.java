package yxlgx.top.gateway.base.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.reactive.HttpHandlerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayClassPathWarningAutoConfiguration;
import org.springframework.cloud.gateway.config.GatewayReactiveLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.DispatcherHandler;
import yxlgx.top.gateway.filter.custom.CustomModifyResponseBodyGatewayFilterFactory;

import java.util.Set;

/**
 * @Author yanxin.
 * @Date 2022/10/12 17:48.
 * Created by IntelliJ IDEA
 * File Description:
 */
@Configuration
public class FilterConfig {
    @Bean
    public CustomModifyResponseBodyGatewayFilterFactory customModifyResponseBodyGatewayFilterFactory(
            ServerCodecConfigurer codecConfigurer, Set<MessageBodyDecoder> bodyDecoders,
            Set<MessageBodyEncoder> bodyEncoders) {
        return new CustomModifyResponseBodyGatewayFilterFactory(codecConfigurer.getReaders(), bodyDecoders, bodyEncoders);
    }
}
