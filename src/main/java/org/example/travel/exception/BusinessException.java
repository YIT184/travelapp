package org.example.travel.exception;

import lombok.Data;

/**
 * 自定义业务异常（如手机号已注册、图片上传失败等）
 */
@Data
public class BusinessException extends RuntimeException {
    private int code;  // 异常状态码

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 400;  // 默认参数错误码
    }
}