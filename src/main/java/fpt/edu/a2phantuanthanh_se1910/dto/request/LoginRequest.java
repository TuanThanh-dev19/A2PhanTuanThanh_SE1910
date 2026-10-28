// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 72) String password) {

    public LoginRequest {
        if (email != null) {
            email = email.trim();
        }
    }
}
