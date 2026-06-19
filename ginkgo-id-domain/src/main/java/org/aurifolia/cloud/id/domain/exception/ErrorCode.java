package org.aurifolia.cloud.id.domain.exception;

/**
 * 通用错误码接口
 * <p>
 * 不同限界上下文可通过实现此接口定义各自的错误码枚举
 *
 * @author Peng Dan
 * @since 2.0
 */
public interface ErrorCode {

    int getCode();

    String getMessage();

    default String format(Object... args) {
        return args.length == 0 ? getMessage() : String.format(getMessage(), args);
    }
}
