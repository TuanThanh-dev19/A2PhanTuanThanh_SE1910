// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record NewsDetail(Long newsArticleId, String newsTitle, String headline,
                         LocalDateTime createdDate, String newsContent, String newsSource,
                         Long categoryId, String categoryName, Integer newsStatus,
                         Long createdById, String createdByName, Long updatedById,
                         String updatedByName, LocalDateTime modifiedDate, List<TagResponse> tags) {
    public record TagResponse(Long tagId, String tagName, String note) {
    }
}
