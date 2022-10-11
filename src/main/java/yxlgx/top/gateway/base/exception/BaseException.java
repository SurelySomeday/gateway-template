package yxlgx.top.gateway.base.exception;

import yxlgx.top.gateway.base.util.GatewayMsgEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yx
 * @date 2021/12/18
 **/
@Data
@EqualsAndHashCode(callSuper = false)
public class BaseException extends RuntimeException{
    private final GatewayMsgEnum msg;

    public BaseException(GatewayMsgEnum msg) {
        super(msg.getCode(), new Throwable(msg.getMsg()));
        this.msg = msg;
    }

}
