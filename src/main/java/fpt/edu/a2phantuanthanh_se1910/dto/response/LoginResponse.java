// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.response;

import java.time.Instant;

public record LoginResponse(String accessToken, String tokenType, Instant expiresAt, AccountResponse account) {
}
