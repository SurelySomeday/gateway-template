package yxlgx.top.gateway.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @Author: yx
 * @Date: 2021/10/12
 **/
@Getter
@Setter
public class GatewayLog {
    /**
     * id
     */
    private String requestId;
    /**
     * http,https
     */
    private String schema;
    /**
     * 请求路径
     */
    private String requestPath;
    /**
     * 请求方法
     */
    private String requestMethod;
    /**
     * 请求参数
     */
    private String requestParam;
    /**
     * 请求body
     */
    private String requestBody;
    /**
     * 响应body
     */
    private String responseData;
    /**
     * 耗时ms
     */
    private long executeTime;

    @Override
    public String toString() {
        return "\nGatewayLog{\n" +
                "requestId=[" + requestId + "]\n" +
                ", schema=[" + schema + "]\n" +
                ", requestPath=[" + requestPath + "]\n" +
                ", requestMethod=[" + requestMethod + "]\n" +
                ", requestParam=[" + requestParam + "]\n" +
                ", requestBody=[" + requestBody + "]\n" +
                ", responseData=[" + responseData + "]\n" +
                ", executeTime=[" + executeTime +
                "]}\n";
    }
}
