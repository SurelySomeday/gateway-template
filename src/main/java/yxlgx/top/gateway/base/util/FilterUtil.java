package yxlgx.top.gateway.base.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Date;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.beans.BeansException;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.buffer.NettyDataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.json.JSONUtil;
import yxlgx.top.gateway.base.constants.Constants;
import yxlgx.top.gateway.base.properties.ProjectGatewayProperties;
import yxlgx.top.gateway.domain.LogPushInfo;

import javax.annotation.Resource;

/**
 * @author yx
 * @date 2021/12/30
 **/
@Component
public class FilterUtil {

    static ProjectGatewayProperties projectGatewayProperties;

    static final MediaType[] mediaTypes=new MediaType[]{MediaType.APPLICATION_JSON,MediaType.APPLICATION_XHTML_XML,MediaType.MULTIPART_FORM_DATA,MediaType.TEXT_HTML,MediaType.TEXT_PLAIN,MediaType.TEXT_XML,MediaType.TEXT_MARKDOWN};

    static final List<MediaType> mediaTypesList;

    static {
        mediaTypesList= Arrays.asList(mediaTypes);
    }


    public static boolean isTargetMediaType(ServerHttpResponse serverHttpResponse) {
        MediaType contentType = serverHttpResponse.getHeaders().getContentType();
        if(contentType==null) return false;
        for(MediaType mediaType:getSupportMediaTypeList()){
            if(contentType.isCompatibleWith(mediaType)){
                return true;
            }
        }
        return false;
    }

    public static boolean isTargetMediaType(MediaType contentType) {
        if(contentType==null) return false;
        for(MediaType mediaType:getSupportMediaTypeList()){
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

    private static List<MediaType> getSupportMediaTypeList(){
        if(projectGatewayProperties==null){
            synchronized (FilterUtil.class){
                if(projectGatewayProperties==null){
                    projectGatewayProperties=SpringUtil.getBean(ProjectGatewayProperties.class);
                }
            }
        }
        List<MediaType> supportMediaList = projectGatewayProperties.getSupportMedia();
        if(supportMediaList!=null&&supportMediaList.isEmpty()){
            return mediaTypesList;
        }
        return supportMediaList;
    }
}
