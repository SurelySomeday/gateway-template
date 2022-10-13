package yxlgx.top.gateway.base.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public enum GatewayMsgEnum {
    /*
     * 错误信息
     *
     */
    SUCCESS("0", "ok"),
    SYSTEM_ERROR("-1", "系统异常，请稍后重试！"),
    SIGNATURE_EXPIRED("20011", "签名过期，请重新生成签名"),
    AUTH_FAILED("20012", "鉴权失败，请重试"),
    SERVICE_NOT_AVAILABLE("20051", "服务不可用");


    private final String code;
    private final String msg;
    private String encodeMsg;
    GatewayMsgEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
        try {
            this.encodeMsg = URLEncoder.encode(msg, StandardCharsets.UTF_8.name());
        }catch (Exception ignored){}
    }
    public String getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
    public String getEncodeMsg() {
        return encodeMsg;
    }
}
