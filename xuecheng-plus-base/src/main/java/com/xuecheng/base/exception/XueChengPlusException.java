package com.xuecheng.base.exception;

/**
 * ClassName: XueChengPlusException
 * Package: com.xuecheng.base.exception
 * Description:
 *
 * @Author: XKing
 * @Create: 2023/5/9 - 8:59
 * @Version: 1.0
 */
public class XueChengPlusException extends RuntimeException{
    private String errMessage;

    public XueChengPlusException(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
