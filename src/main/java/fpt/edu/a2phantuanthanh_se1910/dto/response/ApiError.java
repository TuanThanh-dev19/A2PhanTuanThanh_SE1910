// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.response;

import java.time.Instant;
import java.util.Map;

public record ApiError(int status, String code, String message, Map<String, String> fieldErrors,
                       String path, Instant timestamp) {
    public static ApiError of(int status, String code, String message, String path) {
        return new ApiError(status, code, message, Map.of(), path, Instant.now());
    }
}
