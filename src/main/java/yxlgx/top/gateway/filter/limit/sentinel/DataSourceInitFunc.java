package yxlgx.top.gateway.filter.limit.sentinel;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.cloud.sentinel.custom.SentinelDataSourceHandler;
import com.alibaba.cloud.sentinel.datasource.config.DataSourcePropertiesConfiguration;
import com.alibaba.cloud.sentinel.datasource.config.NacosDataSourceProperties;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.client.config.impl.CacheData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yx
 * @date 2022/01/20
 * @description
 **/
@RefreshScope
@Configuration
public class DataSourceInitFunc {


    @Resource
    SentinelProperties sentinelProperties;

    static Map<String, ReadableDataSource<?, ?>> dataSourceMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void after() throws Exception {
        init();
    }


    public void init() throws Exception {
        Properties flowProperties=new Properties();
        Properties degradeProperties=new Properties();
        //注册GatewayFlowRule 读数据源
        DataSourcePropertiesConfiguration flow = sentinelProperties.getDatasource().get("flow");
        NacosDataSourceProperties flowConfig = flow.getNacos();
        flowProperties.put(PropertyKeyConst.NAMESPACE,flowConfig.getNamespace());
        flowProperties.put(PropertyKeyConst.SERVER_ADDR,flowConfig.getServerAddr());
        ReadableDataSource<String, Set<GatewayFlowRule>> flowRuleDataSource = new NacosDataSource<>(
                flowProperties, flowConfig.getGroupId(), flowConfig.getDataId(),
                source -> JSON.parseObject(source, new TypeReference<Set<GatewayFlowRule>>() {
                }));
        GatewayRuleManager.register2Property(flowRuleDataSource.getProperty());
        dataSourceMap.computeIfPresent("flow", (k, v) -> {
            try {
                v.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        dataSourceMap.put("flow", flowRuleDataSource);
        //注册degrade 读数据源
        DataSourcePropertiesConfiguration degrade = sentinelProperties.getDatasource().get("degrade");
        NacosDataSourceProperties degradeConfig = degrade.getNacos();
        degradeProperties.put(PropertyKeyConst.NAMESPACE,degradeConfig.getNamespace());
        degradeProperties.put(PropertyKeyConst.SERVER_ADDR,degradeConfig.getServerAddr());
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new NacosDataSource<>(
                degradeProperties, degradeConfig.getGroupId(), degradeConfig.getDataId(),
                source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
                }));
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());
        dataSourceMap.computeIfPresent("degrade", (k, v) -> {
            try {
                v.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        dataSourceMap.put("degrade", degradeRuleDataSource);
    }

    @EventListener
    public void envListener(RefreshScopeRefreshedEvent event) {

    }


}
