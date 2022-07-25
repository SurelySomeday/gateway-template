package yxlgx.top.gateway;

import com.alibaba.cloud.sentinel.datasource.config.DataSourcePropertiesConfiguration;
import com.alibaba.cloud.sentinel.datasource.config.NacosDataSourceProperties;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Slf4j
class GatewayApplicationTests {

    @Test
    void contextLoads() {
        Disposable subscribe = Flux.just("a", "b", "c").delayElements(Duration.ofSeconds(1))
                .doOnSubscribe(e -> System.out.println("doOnSubscribe:" + e))
                .doOnNext(item -> System.out.println("next1->" + item))
                .doOnNext(item -> System.out.println("next2->" + item))
                .doOnEach(item -> {
                    System.out.println("each=>" + item.get() + " isOnComplete:" +
                            item.isOnComplete() + " isOnError:" +
                            item.isOnError() + " isOnNext:" +
                            item.isOnNext() + " isOnSubscribe:" +
                            item.isOnSubscribe());
                })
                .doFinally(item -> System.out.println("finally->" + item))
                .doFirst(() -> System.out.println("first"))
                .doOnComplete(() -> System.out.println("complete"))
                .then().subscribe();

        while (!subscribe.isDisposed()){

        }

        WebClient webClient= WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(50 * 1024 * 1024)).build()).build();
        WebClient webClient2= WebClient.builder().exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(50 * 1024 * 1024)).build()).build();
        Mono<String> mono1 = webClient.method(HttpMethod.GET)
                .uri("http://yxlgx.top:8848/nacos/").retrieve()
                .bodyToMono(String.class);
        Mono<String> mono2 = webClient2.method(HttpMethod.GET)
                .uri("http://yxlgx.top:8848/nacos/").retrieve()
                .bodyToMono(String.class);


        System.out.println("================================================");
        long start = System.currentTimeMillis();
        System.out.println();
        Disposable complete = Flux.merge(mono1, mono2)
                .doOnComplete(() -> System.out.println("complete")).subscribe();
        while (!complete.isDisposed()){

        }
        long end = System.currentTimeMillis();
        System.out.println(end-start+"ms");
        System.out.println("================================================");
        Mono<String> mono3 = webClient.method(HttpMethod.GET)
                .uri("http://yxlgx.top:8848/nacos/").retrieve()
                .bodyToMono(String.class);
        Mono<String> mono4 = webClient2.method(HttpMethod.GET)
                .uri("http://172.19.229.75:8848/nacos/").retrieve()
                .bodyToMono(String.class).delayElement(Duration.ofSeconds(10));
        start = System.currentTimeMillis();
        String block3 = mono3.block();
        System.out.println(System.currentTimeMillis()-start+"ms");
        String block4 = mono4.block();
        System.out.println(System.currentTimeMillis()-start+"ms");
        System.out.println(block3.substring(0,10));
        System.out.println(block4.substring(0,10));
        end = System.currentTimeMillis();
        System.out.println(end-start+"ms");
    }

    @Test
    void nacosTest() throws Exception {
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(
                "yxlgx.top:8847", "DEFAULT_GROUP", "aa",
                source -> JSON.parseObject(source, new TypeReference<Set<GatewayFlowRule>>() {
                }));
        String s = degradeRuleDataSource.readSource();
        log.info("sa");
        Thread.sleep(30000);
        degradeRuleDataSource.close();
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource2 = new NacosDataSource<>(
                "yxlgx.top:8848", "DEFAULT_GROUP", "aa",
                source -> JSON.parseObject(source, new TypeReference<Set<GatewayFlowRule>>() {
                }));
        Thread.sleep(300000);
        degradeRuleDataSource2.close();
    }

}
