// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PublicNewsDetail(Long newsArticleId, String newsTitle, String headline,
                               LocalDateTime createdDate, String newsContent, String newsSource,
                               Long categoryId, String categoryName, String authorName,
                               List<TagResponse> tags) {
    public record TagResponse(Long tagId, String tagName) {
    }
}
