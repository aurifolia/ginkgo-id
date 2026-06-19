package org.aurifolia.cloud.id.interfaces.http.handler;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.common.model.Result;
import org.aurifolia.cloud.id.common.exception.IdGenerationException;
import org.aurifolia.cloud.id.common.model.IdResultCode;
import org.aurifolia.cloud.id.domain.exception.DomainException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionSystemException;
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
    public Result<Void> handleDomainException(DomainException e) {
        log.warn("Domain exception: {}", e.getMessage());
        return Result.fail(IdResultCode.DOMAIN_ERROR);
    }

    @ExceptionHandler(IdGenerationException.class)
    public Result<Void> handleIdGenerationException(IdGenerationException e) {
        log.error("ID generation exception: {}", e.getMessage(), e);
        return Result.fail(IdResultCode.ID_GENERATION_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.fail(IdResultCode.PARAM_ERROR, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data integrity violation: {}", e.getMostSpecificCause().getMessage());
        return Result.fail(IdResultCode.DATA_CONFLICT);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public Result<Void> handleTransactionSystemException(TransactionSystemException e) {
        Throwable cause = e.getOriginalException();
        if (cause instanceof DataIntegrityViolationException dive) {
            log.warn("Data integrity violation on commit: {}", dive.getMostSpecificCause().getMessage());
            return Result.fail(IdResultCode.DATA_CONFLICT);
        }
        log.error("Transaction system exception", e);
        return Result.fail(IdResultCode.INTERNAL_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return Result.fail(IdResultCode.INTERNAL_ERROR);
    }
}
