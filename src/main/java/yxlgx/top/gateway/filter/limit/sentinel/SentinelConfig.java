package yxlgx.top.gateway.filter.limit.sentinel;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler;
import com.alibaba.cloud.sentinel.datasource.config.DataSourcePropertiesConfiguration;
import com.alibaba.cloud.sentinel.datasource.config.NacosDataSourceProperties;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import java.util.Properties;

/**
 * @Author yanxin.
 * @Date 2022/7/20 20:02.
 * Created by IntelliJ IDEA
 * File Description:
 */
@Configuration
public class SentinelConfig {

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
