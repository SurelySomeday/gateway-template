package yxlgx.top.gateway.base.util;

public enum GatewayMsgEnum {
    /*
     * 错误信息
     *
     */
    E_0("-0", "ok"),
    E_20001("-1", "系统异常，请稍后重试！"),
    E_20011("20011", "签名过期，请重新生成签名"),
    E_20012("20012", "nonce参数短时间内不能连续重复使用，请重试，谢谢"),
    E_20013("20013", "请求人员用户身份证号异常，请重试"),
    E_20014("20014", "只支持GET、POST"),
    E_20015("20015", "Content-type不能为空"),
    E_20016("20016", "不支持的Content-type:"),
    E_20017("20017", "未获取到对应的服务信息，请确认！"),
    E_20018("20018", "鉴权失败，请重试谢谢"),
    E_20019("20019", "用户凭证票据不能为空！"),
    E_20020("20020", "应用凭证票据不能为空！"),
    E_20021("20021", "资源服务标识resId不能为空！"),
    E_20022("20022", "未获取到站点信息！"),
    E_20023("20023", "未获取到站点信息！请确认！"),
    E_20051("20051", "应用票据异常！请确认！"),
    E_20052("20052", "用户票据异常！请确认！"),
    E_90001("-1", "未知异常！请确认！");


    private String code;
    private String msg;
    GatewayMsgEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public String getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
}
