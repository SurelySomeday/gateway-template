package yxlgx.top.gateway.base.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author yx
 */
@Getter
@Setter
@Component
@ConfigurationProperties("project.gateway")
public class ProjectGatewayProperties {
    /**
     * 是否记录响应体
     */
    private boolean logResponse=false;
    /**
     * 支持的MediaType
     */
    private List<MediaType> supportMedia;
}
