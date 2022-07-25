package yxlgx.top.gateway.base.constants;

/**
 * @Author: yx
 * @Date: 2021/10/13
 **/
public class Constants {
    /**
     * 全局路由，请求开始时间
     */
    public static final String START_TIME="route_start_time";
    /**
     * 全局路由，服务信息
     */
    public static final String SERVICE_INFO="route_service_info";
    /**
     * 全局路由，用户信息
     */
    public static final String USER_INFO="route_user_info";
    /**
     * 全局路由，应用信息
     */
    public static final String APP_INFO="route_app_info";
    /**
     * 全局路由，限流信息
     */
    public static final String LIMIT_INFO="route_limit_ifo";
    /**
     * 全局路由，Sentinel
     */
    public static final String SENTINEL_FLAG="X-Sentinel-Flag";
    /**
     * 全局路由，缓存响应body
     */
    public static final String CACHED_RESPONSE_BODY="route_response_body";
    /**
     * 全局路由，缓存响应body长度
     */
    public static final String CACHED_RESPONSE_BODY_LENGTH="route_response_body_length";
}
