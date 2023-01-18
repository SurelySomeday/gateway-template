package yxlgx.top.gateway.base.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

/**
 * @Author yanxin.
 * @Date 2022/11/8 14:48.
 * Created by IntelliJ IDEA
 * File Description:
 */
@ReactiveFeignClient(name = "web-flux-app")
public interface GreetingReactive {

    @GetMapping("/greeting")
    Mono<String> greeting();

    @GetMapping("/greetingWithParam")
    Mono<String> greetingWithParam(@RequestParam(value = "id") Long id);
}
