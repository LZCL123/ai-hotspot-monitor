package com.example.hotspot.common;

import lombok.Getter;

@Getter
/**
 * 业务异常。
 * 用于主动返回可预期的错误码和错误信息，例如未登录、参数非法或资源不存在。
 */
public class BizException extends RuntimeException {
    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
