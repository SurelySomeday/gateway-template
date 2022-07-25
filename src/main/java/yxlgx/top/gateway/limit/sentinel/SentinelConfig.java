package yxlgx.top.gateway.limit.sentinel;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler;
import com.alibaba.cloud.sentinel.datasource.config.DataSourcePropertiesConfiguration;
import com.alibaba.cloud.sentinel.datasource.config.NacosDataSourceProperties;
import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @Author yanxin.
 * @Date 2022/7/20 20:02.
 * Created by IntelliJ IDEA
 * File Description:
 */
@Configuration
public class SentinelConfig {


    /**
     * 动配置资源
     */
    @PostConstruct
    public void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        //生成一个匹配所有路径的全局apiName，
        // 注意:如果多个匹配，实际上只会让最后一个生效。因此一定要把全局的放在第一位。
        ApiDefinition global = new ApiDefinition("global")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/**")
                            .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(global);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    /**
     * 注入异常处理
     * @return
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelFallbackHandler sentinelGatewayExceptionHandler() {
        return new SentinelFallbackHandler();
    }

    /**
     * 注入全局ConfigService，手动获取更新配置
     * @param sentinelProperties
     * @return
     * @throws Exception
     */
    @RefreshScope
    @Bean(destroyMethod = "shutDown")
    public ConfigService nacosConfigService(SentinelProperties sentinelProperties) throws Exception {
        Properties properties = new Properties();
        DataSourcePropertiesConfiguration nacos = sentinelProperties.getDatasource().get("flow");
        NacosDataSourceProperties nacosConfig = nacos.getNacos();
        //定义IP和端口，如果不指定端口则默认8848
        properties.put(PropertyKeyConst.SERVER_ADDR, nacosConfig.getServerAddr());
        properties.put(PropertyKeyConst.NAMESPACE, nacosConfig.getNamespace());
        return ConfigFactory.createConfigService(properties);
    }

    /**
     * 替换掉SentinelDataSourceHandler，避免注入无意义的数据源 （如果使用web接口，则需要注入。这里是gateway，则无需这个。）
     * @param beanFactory
     * @param sentinelProperties
     * @param env
     * @return
     */
    @Primary
    @Bean
    public SentinelDataSourceHandler sentinelDataSourceHandler(DefaultListableBeanFactory beanFactory, SentinelProperties sentinelProperties, Environment env) {
        return new SentinelDataSourceHandler(beanFactory, sentinelProperties, env){
            @Override
            public void afterSingletonsInstantiated() {
            }
        };
    }
}
