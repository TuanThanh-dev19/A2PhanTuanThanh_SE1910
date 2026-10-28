// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.dto.response;

public record CategoryResponse(Long categoryId, String categoryName, String categoryDescription,
                               Long parentCategoryId, String parentCategoryName, Integer isActive) {
}
