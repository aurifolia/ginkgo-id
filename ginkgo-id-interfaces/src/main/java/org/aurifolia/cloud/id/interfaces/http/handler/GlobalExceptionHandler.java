package org.aurifolia.cloud.id.interfaces.http.handler;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.common.model.Result;
import org.aurifolia.cloud.id.common.exception.IdGenerationException;
import org.aurifolia.cloud.id.domain.exception.DomainException;
import org.aurifolia.cloud.id.interfaces.http.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDomainException(DomainException e) {
        log.warn("领域异常: {}", e.getMessage());
        return new ErrorResponse(e.getErrorCode().getCode(), e.getMessage());
    }

    @ExceptionHandler(IdGenerationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleIdGenerationException(IdGenerationException e) {
        log.error("ID生成异常: {}", e.getMessage(), e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        log.error("未知异常", e);
        return Result.fail("服务器内部错误");
    }
}
