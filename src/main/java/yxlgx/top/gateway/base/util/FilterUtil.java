package yxlgx.top.gateway.base.util;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import yxlgx.top.gateway.base.constants.Constants;
import yxlgx.top.gateway.domain.LogPushInfo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE;

/**
 * @author yx
 * @date 2021/12/30
 **/
public class FilterUtil {
    public static boolean isTargetLogMediaType(ServerHttpResponse serverHttpResponse) {
        MediaType contentType = serverHttpResponse.getHeaders().getContentType();
        if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
            return true;
        } else if (MediaType.APPLICATION_XHTML_XML.isCompatibleWith(contentType)) {
            return true;
        } else if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
            return true;
        } else if (MediaType.TEXT_HTML.isCompatibleWith(contentType)) {
            return true;
        } else if (MediaType.TEXT_PLAIN.isCompatibleWith(contentType)) {
            return true;
        } else if (MediaType.TEXT_XML.isCompatibleWith(contentType)) {
            return true;
        } else if (MediaType.TEXT_MARKDOWN.isCompatibleWith(contentType)) {
            return true;
        }

        return false;

    }

    public static LogPushInfo generateLog(ServerWebExchange exchange) {
        //记录日志的对象
        //从Attribute中获取cachedBodyFilter缓存下来的请求体
        Object cachedBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
        JSONObject userInfo = exchange.getAttribute(Constants.USER_INFO);
        String requestBodyStr = null;
        //处理body保存
        if (null != cachedBody) {
            NettyDataBuffer buffer = (NettyDataBuffer) cachedBody;
            //转换为string(可能存在编码问题)
            requestBodyStr = buffer.toString();
        }
        LogPushInfo logPushInfo = new LogPushInfo();
        //获取开始请求的时间
        if (exchange.getAttributes().containsKey(Constants.START_TIME)) {
            long startTime = (long) exchange.getAttributes().get(Constants.START_TIME);
            logPushInfo.setCallTime(new Date(startTime));
        }
        if (userInfo != null) {
            logPushInfo.setUserId(userInfo.getString("userId"));
        }
        String id = exchange.getRequest().getId();
        logPushInfo.setRequestBody(requestBodyStr);
        logPushInfo.setId(id);
        logPushInfo.setMethod(exchange.getRequest().getMethodValue());
        logPushInfo.setError(false);
        logPushInfo.setRequestHeader(exchange.getRequest().getHeaders().toString());
        logPushInfo.setRequestParam(JSONUtil.toJsonStr(exchange.getRequest().getQueryParams()));
        logPushInfo.setResponseHeader(exchange.getResponse().getHeaders().toString());
        //logPushInfo.setResponseContent(originalBody.toString());
        logPushInfo.setResponseTime(new Date());
        return logPushInfo;
    }
}
