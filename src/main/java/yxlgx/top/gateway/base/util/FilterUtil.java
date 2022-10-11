package yxlgx.top.gateway.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.json.JSONUtil;
import yxlgx.top.gateway.base.constants.Constants;
import yxlgx.top.gateway.domain.LogPushInfo;

/**
 * @author yx
 * @date 2021/12/30
 **/
public class FilterUtil {
    public static boolean isTargetLogMediaType(ServerHttpResponse serverHttpResponse) {
        MediaType contentType = serverHttpResponse.getHeaders().getContentType();
        if(contentType==null) return false;
        List<MediaType> mediaTypes=new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.APPLICATION_XHTML_XML);
        mediaTypes.add(MediaType.MULTIPART_FORM_DATA);
        mediaTypes.add(MediaType.TEXT_HTML);
        mediaTypes.add(MediaType.TEXT_PLAIN);
        mediaTypes.add(MediaType.TEXT_XML);
        mediaTypes.add(MediaType.TEXT_MARKDOWN);
        for(MediaType mediaType:mediaTypes){
            if(contentType.isCompatibleWith(mediaType)){
                return true;
            }
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
