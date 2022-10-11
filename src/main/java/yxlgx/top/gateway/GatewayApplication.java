package yxlgx.top.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.scheduling.annotation.EnableScheduling;

@Import(cn.hutool.extra.spring.SpringUtil.class)
@EnableScheduling
@EnableMessageHistory
@SpringBootApplication(scanBasePackages = {"cn.hutool.extra.spring", "yxlgx.top"},
        exclude = {DataSourceAutoConfiguration.class} )
public class GatewayApplication {

    public static void main(String[] args) {
        System.setProperty("csp.sentinel.app.type","11");
        SpringApplication.run(GatewayApplication.class, args);
    }

}
