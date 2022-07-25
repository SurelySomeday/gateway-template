package yxlgx.top.gateway.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 限流信息
 * @author yx
 * @date 2021/12/28
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LimitInfo {
    private String key;
    private String appId;
    private String serviceId;
    private String userId;
    private String callFrequency;
    private Long timeOut;
}
