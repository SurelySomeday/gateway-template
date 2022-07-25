package yxlgx.top.gateway.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author yx
 * @date 2021/12/16
 **/
@Data
public class LogPushInfo {
    /**
     * 主键id
     */
    private String id;
    /**
     * 调用人员
     */
    private String userId;
    /**
     * 请求方法
     */
    private String method;
    /**
     * 调用时间
     */
    private Date callTime;
    /**
     * 请求体大小
     */
    private long requestLength= 0L;
    /**
     * 响应体大小
     */
    private long responseLength= 0L;
    /**
     * 请求参数
     */
    private String requestParam;
    /**
     * 请求body
     */
    private String requestBody;
    /**
     * 请求头
     */
    private String requestHeader;
    /**
     * 响应时间
     */
    private Date responseTime;
    /**
     * 调用时长
     */
    private BigDecimal callDuration;
    /**
     * 是否异常
     */
    private boolean isError;
    /**
     * 异常信息
     */
    private String errorInfo;
    /**
     * 响应内容
     */
    private String responseContent;
    /**
     * 响应头
     */
    private String responseHeader;
}
