package yxlgx.top.gateway.filter.response;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.factory.rewrite.GzipMessageBodyResolver;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import yxlgx.top.gateway.base.constants.Constants;
import yxlgx.top.gateway.base.properties.ProjectGatewayProperties;
import yxlgx.top.gateway.base.util.FilterUtil;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yx
 * @date 2021/12/28
 * @description 可重写响应体
 **/
@Component
@Slf4j
public class ResponseBodyRewriteFunction implements RewriteFunction<byte[], byte[]> {

    Map<String, MessageBodyDecoder> messageBodyDecoders;
    Map<String, MessageBodyEncoder> messageBodyEncoders;
    @Autowired
    ApplicationContext applicationContext;
    @Resource
    ProjectGatewayProperties projectGatewayProperties;

    public ResponseBodyRewriteFunction() {
        GzipMessageBodyResolver gzipMessageBodyResolver = new GzipMessageBodyResolver();
        this.messageBodyDecoders = new HashMap<>();
        this.messageBodyEncoders = new HashMap<>();
        this.messageBodyDecoders.put(gzipMessageBodyResolver.encodingType(), gzipMessageBodyResolver);
        this.messageBodyEncoders.put(gzipMessageBodyResolver.encodingType(), gzipMessageBodyResolver);


    }

    @Override
    public Publisher<byte[]> apply(ServerWebExchange exchange, byte[] responseBytes) {
        try {
            if (ServerWebExchangeUtils.isAlreadyRouted(exchange)) {
                //根据contentType判断需要拦截处理的数据,一般可以拦截json和text/plain等类型,若是字节数据可能会有异常
                //下面代码也可以修改响应头或者body
                if (FilterUtil.isTargetMediaType(exchange.getResponse())&& projectGatewayProperties.isLogResponse()) {
                    String extractBody = extractBody(exchange, responseBytes);
                    exchange.getAttributes().put(Constants.CACHED_RESPONSE_BODY, extractBody);
                    if (responseBytes != null) {
                        exchange.getAttributes().put(Constants.CACHED_RESPONSE_BODY_LENGTH, responseBytes.length);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (responseBytes == null) return Mono.empty();
        return Mono.just(responseBytes);
    }

    private String extractBody(ServerWebExchange exchange, byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        List<String> encodingHeaders = exchange.getResponse().getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
        MediaType contentType = exchange.getResponse().getHeaders().getContentType();
        for (String encoding : encodingHeaders) {
            MessageBodyDecoder decoder = messageBodyDecoders.get(encoding);
            if (decoder != null) {
                //检查编码
                if(contentType!=null&&contentType.getCharset()!=null){
                    return new String(bytes,contentType.getCharset());
                }else {
                    return new String(bytes);
                }
            }
        }
        //检查编码
        if(contentType!=null&&contentType.getCharset()!=null){
            return new String(bytes,contentType.getCharset());
        }
        return new String(bytes);
    }


}


