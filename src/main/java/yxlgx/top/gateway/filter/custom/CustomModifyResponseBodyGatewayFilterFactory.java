package yxlgx.top.gateway.filter.custom;

import static java.util.function.Function.identity;
import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyDecoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.MessageBodyEncoder;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import yxlgx.top.gateway.base.util.FilterUtil;

/**
 * @Author yanxin.
 * @Date 2022/10/12 17:36.
 * Created by IntelliJ IDEA
 * File Description:
 */
public class CustomModifyResponseBodyGatewayFilterFactory
        extends AbstractGatewayFilterFactory<CustomModifyResponseBodyGatewayFilterFactory.Config> {

    private final Map<String, MessageBodyDecoder> messageBodyDecoders;

    private final Map<String, MessageBodyEncoder> messageBodyEncoders;

    private final List<HttpMessageReader<?>> messageReaders;

    public CustomModifyResponseBodyGatewayFilterFactory(List<HttpMessageReader<?>> messageReaders,
                                                  Set<MessageBodyDecoder> messageBodyDecoders, Set<MessageBodyEncoder> messageBodyEncoders) {
        super(CustomModifyResponseBodyGatewayFilterFactory.Config.class);
        this.messageReaders = messageReaders;
        this.messageBodyDecoders = messageBodyDecoders.stream()
                .collect(Collectors.toMap(MessageBodyDecoder::encodingType, identity()));
        this.messageBodyEncoders = messageBodyEncoders.stream()
                .collect(Collectors.toMap(MessageBodyEncoder::encodingType, identity()));
    }

    @Override
    public GatewayFilter apply(CustomModifyResponseBodyGatewayFilterFactory.Config config) {
        CustomModifyResponseBodyGatewayFilterFactory.ModifyResponseGatewayFilter gatewayFilter = new CustomModifyResponseBodyGatewayFilterFactory.ModifyResponseGatewayFilter(config);
        gatewayFilter.setFactory(this);
        return gatewayFilter;
    }

    public static class Config {

        private Class inClass;

        private Class outClass;

        private Map<String, Object> inHints;

        private Map<String, Object> outHints;

        private String newContentType;

        private RewriteFunction rewriteFunction;

        public Class getInClass() {
            return inClass;
        }

        public CustomModifyResponseBodyGatewayFilterFactory.Config setInClass(Class inClass) {
            this.inClass = inClass;
            return this;
        }

        public Class getOutClass() {
            return outClass;
        }

        public CustomModifyResponseBodyGatewayFilterFactory.Config setOutClass(Class outClass) {
            this.outClass = outClass;
            return this;
        }

        public Map<String, Object> getInHints() {
            return inHints;
        }

        public CustomModifyResponseBodyGatewayFilterFactory.Config setInHints(Map<String, Object> inHints) {
            this.inHints = inHints;
            return this;
        }

        public Map<String, Object> getOutHints() {
            return outHints;
        }

        public CustomModifyResponseBodyGatewayFilterFactory.Config setOutHints(Map<String, Object> outHints) {
            this.outHints = outHints;
            return this;
        }

        public String getNewContentType() {
            return newContentType;
        }

        public CustomModifyResponseBodyGatewayFilterFactory.Config setNewContentType(String newContentType) {
            this.newContentType = newContentType;
            return this;
        }

        public RewriteFunction getRewriteFunction() {
            return rewriteFunction;
        }

        public CustomModifyResponseBodyGatewayFilterFactory.Config setRewriteFunction(RewriteFunction rewriteFunction) {
            this.rewriteFunction = rewriteFunction;
            return this;
        }

        public <T, R> CustomModifyResponseBodyGatewayFilterFactory.Config setRewriteFunction(Class<T> inClass, Class<R> outClass,
                                                                                       RewriteFunction<T, R> rewriteFunction) {
            setInClass(inClass);
            setOutClass(outClass);
            setRewriteFunction(rewriteFunction);
            return this;
        }

    }

    public class ModifyResponseGatewayFilter implements GatewayFilter, Ordered {

        private final CustomModifyResponseBodyGatewayFilterFactory.Config config;

        private GatewayFilterFactory<CustomModifyResponseBodyGatewayFilterFactory.Config> gatewayFilterFactory;

        public ModifyResponseGatewayFilter(CustomModifyResponseBodyGatewayFilterFactory.Config config) {
            this.config = config;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            return chain.filter(exchange.mutate().response(new CustomModifyResponseBodyGatewayFilterFactory.ModifiedServerHttpResponse(exchange, config)).build());
        }

        @Override
        public int getOrder() {
            return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
        }

        @Override
        public String toString() {
            Object obj = (this.gatewayFilterFactory != null) ? this.gatewayFilterFactory : this;
            return filterToStringCreator(obj).append("New content type", config.getNewContentType())
                    .append("In class", config.getInClass()).append("Out class", config.getOutClass()).toString();
        }

        public void setFactory(GatewayFilterFactory<CustomModifyResponseBodyGatewayFilterFactory.Config> gatewayFilterFactory) {
            this.gatewayFilterFactory = gatewayFilterFactory;
        }

    }

    protected class ModifiedServerHttpResponse extends ServerHttpResponseDecorator {

        private final ServerWebExchange exchange;

        private final CustomModifyResponseBodyGatewayFilterFactory.Config config;

        public ModifiedServerHttpResponse(ServerWebExchange exchange, CustomModifyResponseBodyGatewayFilterFactory.Config config) {
            super(exchange.getResponse());
            this.exchange = exchange;
            this.config = config;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            //检查是不是需要缓存body的
            String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
            if(StringUtils.isBlank(originalResponseContentType)){
                return super.writeWith(body);
            }else{
                MediaType mediaType=null;
                try {
                    mediaType = MediaType.valueOf(originalResponseContentType);
                }catch (Exception ignore){
                }
                if(!FilterUtil.isTargetMediaType(mediaType)){
                    return super.writeWith(body);
                }
            }

            Class inClass = config.getInClass();
            Class outClass = config.getOutClass();

            HttpHeaders httpHeaders = new HttpHeaders();
            // explicitly add it in this way instead of
            // 'httpHeaders.setContentType(originalResponseContentType)'
            // this will prevent exception in case of using non-standard media
            // types like "Content-Type: image"
            httpHeaders.add(HttpHeaders.CONTENT_TYPE, originalResponseContentType);

            ClientResponse clientResponse = prepareClientResponse(body, httpHeaders);

            // TODO: flux or mono
            Mono modifiedBody = extractBody(exchange, clientResponse, inClass)
                    .flatMap(originalBody -> config.getRewriteFunction().apply(exchange, originalBody))
                    .switchIfEmpty(Mono.defer(() -> (Mono) config.getRewriteFunction().apply(exchange, null)));

            BodyInserter bodyInserter = BodyInserters.fromPublisher(modifiedBody, outClass);
            CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange,
                    exchange.getResponse().getHeaders());
            return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
                Mono<DataBuffer> messageBody = writeBody(getDelegate(), outputMessage, outClass);
                HttpHeaders headers = getDelegate().getHeaders();
                if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)
                        || headers.containsKey(HttpHeaders.CONTENT_LENGTH)) {
                    messageBody = messageBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
                }
                // TODO: fail if isStreamingMediaType?
                return getDelegate().writeWith(messageBody);
            }));
        }

        @Override
        public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
            //检查是不是需要缓存body的
            String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
            if(StringUtils.isBlank(originalResponseContentType)){
                return super.writeAndFlushWith(body);
            }else{
                MediaType mediaType=null;
                try {
                    mediaType = MediaType.valueOf(originalResponseContentType);
                }catch (Exception ignore){
                }
                if(!FilterUtil.isTargetMediaType(mediaType)){
                    return super.writeAndFlushWith(body);
                }
            }
            return writeWith(Flux.from(body).flatMapSequential(p -> p));
        }

        private ClientResponse prepareClientResponse(Publisher<? extends DataBuffer> body, HttpHeaders httpHeaders) {
            ClientResponse.Builder builder;
            builder = ClientResponse.create(exchange.getResponse().getStatusCode(), messageReaders);
            return builder.headers(headers -> headers.putAll(httpHeaders)).body(Flux.from(body)).build();
        }

        private <T> Mono<T> extractBody(ServerWebExchange exchange, ClientResponse clientResponse, Class<T> inClass) {
            // if inClass is byte[] then just return body, otherwise check if
            // decoding required
            if (byte[].class.isAssignableFrom(inClass)) {
                return clientResponse.bodyToMono(inClass);
            }

            List<String> encodingHeaders = exchange.getResponse().getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
            for (String encoding : encodingHeaders) {
                MessageBodyDecoder decoder = messageBodyDecoders.get(encoding);
                if (decoder != null) {
                    return clientResponse.bodyToMono(byte[].class).publishOn(Schedulers.parallel()).map(decoder::decode)
                            .map(bytes -> exchange.getResponse().bufferFactory().wrap(bytes))
                            .map(buffer -> prepareClientResponse(Mono.just(buffer),
                                    exchange.getResponse().getHeaders()))
                            .flatMap(response -> response.bodyToMono(inClass));
                }
            }

            return clientResponse.bodyToMono(inClass);
        }

        private Mono<DataBuffer> writeBody(ServerHttpResponse httpResponse, CachedBodyOutputMessage message,
                                           Class<?> outClass) {
            Mono<DataBuffer> response = DataBufferUtils.join(message.getBody());
            if (byte[].class.isAssignableFrom(outClass)) {
                return response;
            }

            List<String> encodingHeaders = httpResponse.getHeaders().getOrEmpty(HttpHeaders.CONTENT_ENCODING);
            for (String encoding : encodingHeaders) {
                MessageBodyEncoder encoder = messageBodyEncoders.get(encoding);
                if (encoder != null) {
                    DataBufferFactory dataBufferFactory = httpResponse.bufferFactory();
                    response = response.publishOn(Schedulers.parallel()).map(buffer -> {
                        byte[] encodedResponse = encoder.encode(buffer);
                        DataBufferUtils.release(buffer);
                        return encodedResponse;
                    }).map(dataBufferFactory::wrap);
                    break;
                }
            }

            return response;
        }

    }

}
