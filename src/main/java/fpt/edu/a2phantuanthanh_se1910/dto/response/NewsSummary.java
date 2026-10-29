// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.response;

import java.time.LocalDateTime;

public record NewsSummary(Long newsArticleId, String newsTitle, String headline,
                          LocalDateTime createdDate, Long categoryId, String categoryName,
                          Integer newsStatus, Long createdById, String createdByName,
                          LocalDateTime modifiedDate) {
}
