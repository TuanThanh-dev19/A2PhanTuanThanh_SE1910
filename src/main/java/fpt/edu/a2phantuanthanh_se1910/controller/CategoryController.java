// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.controller;

import fpt.edu.a2phantuanthanh_se1910.dto.response.CategoryResponse;
import fpt.edu.a2phantuanthanh_se1910.dto.request.CategoryRequest;
import fpt.edu.a2phantuanthanh_se1910.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.net.URI;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> search(@RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(categoryService.search(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable @Positive long id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(categoryService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.created(URI.create("/api/categories/" + response.categoryId()))
                .cacheControl(CacheControl.noStore()).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable @Positive long id,
                                                  @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(categoryService.update(id, request));
    }
}
