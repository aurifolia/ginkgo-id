package org.aurifolia.cloud.id.metaserver.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * <p>
 * 统一处理Controller层抛出的异常，返回标准化的错误响应
 *
 * @author Peng Dan
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理ID元数据服务异常
     */
    @ExceptionHandler(IdMetaException.class)
    public ResponseEntity<Map<String, Object>> handleIdMetaException(IdMetaException e) {
        log.error("ID元数据服务异常: {}", e.getMessage(), e);
        
        Map<String, Object> error = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            e.getMessage(),
            "ID_META_ERROR"
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        
        Map<String, Object> error = buildErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            e.getMessage(),
            "INVALID_ARGUMENT"
        );
        
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * 处理其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        log.error("系统内部错误", e);
        
        Map<String, Object> error = buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "系统内部错误，请稍后重试",
            "INTERNAL_ERROR"
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    /**
     * 构建错误响应体
     */
    private Map<String, Object> buildErrorResponse(int status, String message, String errorCode) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", status);
        error.put("error", HttpStatus.valueOf(status).getReasonPhrase());
        error.put("message", message);
        error.put("errorCode", errorCode);
        return error;
    }
}
