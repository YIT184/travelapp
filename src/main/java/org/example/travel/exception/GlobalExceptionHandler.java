package org.example.travel.exception;

import org.example.travel.model.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理，统一返回错误结果
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. 处理参数校验异常（MethodArgumentNotValidException）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultVO<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("参数校验异常：", e);
        // 获取参数校验的所有错误信息（用分号分隔）
        BindingResult bindingResult = e.getBindingResult();
        String errorMsg = bindingResult.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        // 调用你ResultVO的error方法，返回400 + 具体错误信息
        return ResultVO.error(400, errorMsg);
    }

    // 2. 处理自定义业务异常（原有逻辑）
    @ExceptionHandler(BusinessException.class)
    public ResultVO<Void> handleBusinessException(BusinessException e) {
        log.error("业务异常：", e);
        return ResultVO.error(e.getCode(), e.getMessage());
    }

    // 3. 处理通用运行时异常（原有逻辑）
    @ExceptionHandler(RuntimeException.class)
    public ResultVO<Void> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常：", e);
        return ResultVO.error(500, e.getMessage());
    }

    // 4. 处理所有未捕获的异常（兜底，原有逻辑）
    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return ResultVO.serverError();
    }
}