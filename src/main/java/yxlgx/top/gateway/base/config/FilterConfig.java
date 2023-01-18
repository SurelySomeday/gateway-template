package yxlgx.top.gateway.base.config;

import java.util.Set;

import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;

import yxlgx.top.gateway.filter.custom.CustomModifyResponseBodyGatewayFilterFactory;

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
