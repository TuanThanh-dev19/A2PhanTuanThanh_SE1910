// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.response.CategoryResponse;
import fpt.edu.a2phantuanthanh_se1910.dto.request.CategoryRequest;
import fpt.edu.a2phantuanthanh_se1910.entity.Category;
import fpt.edu.a2phantuanthanh_se1910.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    public List<CategoryResponse> search(String query) {
        String keyword = query.trim();
        if (keyword.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search must be at most 200 characters");
        }
        // Treat user text literally, including SQL Server LIKE wildcards.
        keyword = keyword.toLowerCase(Locale.ROOT).replace("!", "!!")
                .replace("%", "!%").replace("_", "!_").replace("[", "![");
        List<Category> matches = categories.search(keyword);
        List<CategoryResponse> response = new ArrayList<>();
        for (Category category : matches) {
            response.add(mapCategory(category));
        }
        return response;
    }

    public CategoryResponse findById(long id) {
        Category category = categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        return mapCategory(category);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category parent = findParent(request.parentCategoryId());
        Category category = new Category(request.categoryName(), request.categoryDescription(),
                parent, request.isActive());
        categories.saveAndFlush(category);
        return mapCategory(category);
    }

    private Category findParent(Long parentId) {
        if (parentId == null) return null;
        return categories.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Danh mục cha không tồn tại. Vui lòng chọn lại."));
    }

    private CategoryResponse mapCategory(Category category) {
        Long parentId = null;
        String parentName = null;
        Category parent = category.getParentCategory();
        if (parent != null) {
            parentId = parent.getCategoryId();
            parentName = parent.getCategoryName();
        }
        return new CategoryResponse(category.getCategoryId(), category.getCategoryName(),
                category.getCategoryDescription(), parentId, parentName, category.getIsActive());
    }
}
