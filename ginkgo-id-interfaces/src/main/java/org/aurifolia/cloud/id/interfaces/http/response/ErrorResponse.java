package org.aurifolia.cloud.id.interfaces.http.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错误响应（含错误码）
 *
 * @author Peng Dan
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private Integer code;

    private String message;
}
