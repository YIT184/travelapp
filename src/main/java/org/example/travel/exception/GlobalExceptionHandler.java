package org.example.travel.exception;

import org.example.travel.model.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，统一返回错误结果
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 处理所有运行时异常
    @ExceptionHandler(RuntimeException.class)
    public ResultVO<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常：", e);
        return ResultVO.error(500, e.getMessage());
    }

    // 处理自定义业务异常（后续可扩展）
    @ExceptionHandler(BusinessException.class)
    public ResultVO<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：", e);
        return ResultVO.error(e.getCode(), e.getMessage());
    }

    // 处理其他异常
    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return ResultVO.serverError();
    }
}