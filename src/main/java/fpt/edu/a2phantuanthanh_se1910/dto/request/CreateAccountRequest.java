// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record CreateAccountRequest(
        @NotBlank @Size(max = 100) String accountName,
        @NotBlank @Email @Size(max = 254) String accountEmail,
        @NotNull @Min(1) @Max(2) Integer accountRole,
        @NotBlank @Size(max = 72) String password) {

    public CreateAccountRequest {
        if (accountName != null) accountName = accountName.trim();
        if (accountEmail != null) accountEmail = accountEmail.trim().toLowerCase(Locale.ROOT);
    }
}
