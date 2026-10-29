// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record NewsRequest(
        @NotBlank @Size(max = 200) String newsTitle,
        @NotBlank @Size(max = 300) String headline,
        @NotBlank @Size(max = 20000) String newsContent,
        @Size(max = 500) String newsSource,
        @NotNull @Positive Long categoryId,
        @NotNull @Min(0) @Max(1) Integer newsStatus,
        @NotNull List<@NotNull @Valid TagInput> tags) {
    public NewsRequest {
        if (newsTitle != null) newsTitle = newsTitle.trim();
        if (headline != null) headline = headline.trim();
        if (newsContent != null) newsContent = newsContent.trim();
        if (newsSource != null) newsSource = newsSource.trim();
    }

    public record TagInput(@Positive Long tagId, @NotBlank @Size(max = 100) String tagName,
                           @Size(max = 500) String note) {
        public TagInput {
            if (tagName != null) tagName = tagName.trim();
            if (note != null) note = note.trim();
        }
    }
}
