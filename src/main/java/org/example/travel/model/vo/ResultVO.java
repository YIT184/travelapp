package org.example.travel.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 全局统一返回结果
 */
@Data
@NoArgsConstructor
public class ResultVO<T> {
    private int code;       // 状态码（200成功，400参数错误，401未授权，500服务器错误）
    private String msg;     // 提示信息
    private T data;         // 业务数据

    // 私有构造函数，用于静态方法
    private ResultVO(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功响应（带数据）
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "操作成功", data);
    }

    // 成功响应（无数据）
    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "操作成功", null);
    }

    // 失败响应
    public static <T> ResultVO<T> error(int code, String msg) {
        return new ResultVO<>(code, msg, null);
    }

    // 常用失败场景
    public static <T> ResultVO<T> paramError() {
        return new ResultVO<>(400, "参数错误", null);
    }

    public static <T> ResultVO<T> unAuth() {
        return new ResultVO<>(401, "登录已过期，请重新登录", null);
    }

    public static <T> ResultVO<T> serverError() {
        return new ResultVO<>(500, "服务器内部错误", null);
    }
}