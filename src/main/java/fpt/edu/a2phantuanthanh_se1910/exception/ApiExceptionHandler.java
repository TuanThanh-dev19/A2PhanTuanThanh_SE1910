// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.exception;

import fpt.edu.a2phantuanthanh_se1910.dto.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> badCredentials(HttpServletRequest request) {
        return ResponseEntity.status(401).body(ApiError.of(401, "UNAUTHORIZED",
                "Email or password is incorrect", request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException exception,
                                               HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (var error : exception.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new ApiError(400, "VALIDATION_ERROR",
                "Invalid request fields", fields, request.getRequestURI(), Instant.now()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class})
    public ResponseEntity<ApiError> invalidRequest(HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiError.of(400, "VALIDATION_ERROR",
                "Invalid request value or format", request.getRequestURI()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> statusError(ResponseStatusException exception, HttpServletRequest request) {
        int status = exception.getStatusCode().value();
        String code = "REQUEST_ERROR";
        if (status == 404) {
            code = "NOT_FOUND";
        } else if (status == 400) {
            code = "VALIDATION_ERROR";
        } else if (status == 409) {
            code = "CONFLICT";
        }
        return ResponseEntity.status(status).body(ApiError.of(status, code,
                exception.getReason(), request.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> dataConflict(HttpServletRequest request) {
        return ResponseEntity.status(409).body(ApiError.of(409, "CONFLICT",
                "Data conflicts with an existing account or news reference. Reload and try again.",
                request.getRequestURI()));
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ApiError> concurrentChange(HttpServletRequest request) {
        return ResponseEntity.status(409).body(ApiError.of(409, "CONFLICT",
                "Dữ liệu đang được cập nhật. Vui lòng tải lại và thử lại.", request.getRequestURI()));
    }
}
