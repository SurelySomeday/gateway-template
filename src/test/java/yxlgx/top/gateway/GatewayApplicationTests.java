package yxlgx.top.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
                .uri("http://172.19.229.75:8848/nacos/").retrieve()
                .bodyToMono(String.class);

        System.out.println("===================== blockAll ===========================");
        long start = System.currentTimeMillis();
        System.out.println();
        String blockLast = Flux.merge(mono1, mono2)
                .doOnComplete(() -> System.out.println("complete")).blockLast();
        long end = System.currentTimeMillis();
        System.out.println(end-start+"ms");
        System.out.println("====================== blockOne ==========================");
        Mono<String> mono3 = webClient.method(HttpMethod.GET)
                .uri("http://yxlgx.top:8848/nacos/").retrieve()
                .bodyToMono(String.class);
        Mono<String> mono4 = webClient2.method(HttpMethod.GET)
                .uri("http://172.19.229.75:8848/nacos/").retrieve()
                .bodyToMono(String.class);
        start = System.currentTimeMillis();
        String block3 = mono3.block();
        long mid = System.currentTimeMillis();
        System.out.println(mid-start+"ms");
        String block4 = mono4.block();
        System.out.println(System.currentTimeMillis()-mid+"ms");
        end = System.currentTimeMillis();
        System.out.println(end-start+"ms");
    }

    @Test
    void nacosTest() throws Exception {
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(
                "yxlgx.top:8848", "DEFAULT_GROUP", "aa",
                source -> JSON.parseObject(source, new TypeReference<Set<GatewayFlowRule>>() {
                }));
        String s = degradeRuleDataSource.readSource();
        log.info(s);
        Thread.sleep(30000);
        degradeRuleDataSource.close();
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource2 = new NacosDataSource<>(
                "yxlgx.top:8848", "DEFAULT_GROUP", "aa",
                source -> JSON.parseObject(source, new TypeReference<Set<GatewayFlowRule>>() {
                }));
        Thread.sleep(300000);
        degradeRuleDataSource2.close();
    }

    @Test
    void testNaming() throws NacosException {
        Properties properties = new Properties();
        properties.put("serverAddr", "yxlgx.top:8848");
        properties.put("namespace", "dev");
        NamingService namingService = NamingFactory.createNamingService(properties);
        namingService.subscribe("gateway", "DEFAULT_GROUP", event -> {
            List<Instance> allInstances = null;
            try {
                //获取所有该服务的列表
                allInstances = namingService.getAllInstances("gateway");
                allInstances.stream().forEach(item->{
                    System.out.println(item.getIp());
                });
            }catch (Exception e){

            }
        });
        List<Instance> gateway = namingService.getAllInstances("gateway");
        gateway.stream().forEach(item->{
            System.out.println(item.toString());
        });

    }

    @Test
    public void testSimple() throws UnsupportedEncodingException {
        byte[] bytes=new byte[]{0,0,0,114};
        System.out.println(new String(bytes, StandardCharsets.US_ASCII));

    }

}
