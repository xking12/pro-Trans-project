package com.xuecheng.base.exception;

import java.io.Serializable;

/**
 * ClassName: RestErrorResponse
 * Package: com.xuecheng.base.exception
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/9 - 9:01
 * @Version: 1.0
 */
public class RestErrorResponse implements Serializable {
    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
