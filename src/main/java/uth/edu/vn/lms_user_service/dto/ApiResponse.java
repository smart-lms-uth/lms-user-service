package uth.edu.vn.lms_user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Standard API Response wrapper for all endpoints
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    int status,
    boolean success,
    String message,
    T data,
    ErrorDetails error,
    Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, true, "Success", data, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, true, message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, true, message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(int status, String message, String errorCode) {
        return new ApiResponse<>(status, false, message, null, 
            new ErrorDetails(errorCode, message, null), Instant.now());
    }

    public static <T> ApiResponse<T> error(int status, String message, String errorCode, String details) {
        return new ApiResponse<>(status, false, message, null, 
            new ErrorDetails(errorCode, message, details), Instant.now());
    }

    public record ErrorDetails(
        String code,
        String message,
        String details
    ) {}
}
