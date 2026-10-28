// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 100) String categoryName,
        @Size(max = 1000) String categoryDescription,
        @Positive Long parentCategoryId,
        @NotNull @Min(0) @Max(1) Integer isActive) {

    public CategoryRequest {
        if (categoryName != null) categoryName = categoryName.trim();
        if (categoryDescription != null) categoryDescription = categoryDescription.trim();
    }
}
