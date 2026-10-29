// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.response.CategoryResponse;
import fpt.edu.a2phantuanthanh_se1910.dto.request.CategoryRequest;
import fpt.edu.a2phantuanthanh_se1910.entity.Category;
import fpt.edu.a2phantuanthanh_se1910.repository.CategoryRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsArticleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categories;
    private final NewsArticleRepository newsArticles;

    public CategoryService(CategoryRepository categories, NewsArticleRepository newsArticles) {
        this.categories = categories;
        this.newsArticles = newsArticles;
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
        return mapCategory(requireCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category parent = findParent(request.parentCategoryId(), null);
        Category category = new Category(request.categoryName(), request.categoryDescription(),
                parent, request.isActive());
        categories.saveAndFlush(category);
        return mapCategory(category);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public CategoryResponse update(long id, CategoryRequest request) {
        Category category = requireCategory(id);
        Category parent = findParent(request.parentCategoryId(), id);
        category.setCategoryName(request.categoryName());
        category.setCategoryDescription(request.categoryDescription());
        category.setParentCategory(parent);
        category.setIsActive(request.isActive());
        categories.saveAndFlush(category);
        return mapCategory(category);
    }

    @Transactional
    public void delete(long id) {
        Category category = requireCategory(id);
        if (newsArticles.existsByCategory_CategoryId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete a category used by articles, including inactive articles.");
        }
        List<Category> children = categories.findByParentCategory_CategoryId(id);
        for (Category child : children) {
            child.setParentCategory(null);
        }
        // DEC-05: release child references before deleting; any failure rolls back both steps.
        categories.flush();
        categories.delete(category);
        categories.flush();
    }

    private Category requireCategory(long id) {
        return categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private Category findParent(Long parentId, Long categoryId) {
        if (parentId == null) return null;
        if (parentId.equals(categoryId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A category cannot be its own parent.");
        }
        Category parent = categories.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "The parent category no longer exists. Please select another category."));
        Set<Long> visitedIds = new HashSet<>();
        Category ancestor = parent;
        // Keep the hierarchy stable until save; also stop if existing data already contains a cycle.
        while (ancestor != null) {
            Long ancestorId = ancestor.getCategoryId();
            if (ancestorId.equals(categoryId) || !visitedIds.add(ancestorId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected parent would create a category cycle.");
            }
            ancestor = ancestor.getParentCategory();
        }
        return parent;
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
