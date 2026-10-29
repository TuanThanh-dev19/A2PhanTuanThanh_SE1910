// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.controller;

import fpt.edu.a2phantuanthanh_se1910.dto.response.TagResponse;
import fpt.edu.a2phantuanthanh_se1910.service.TagService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) { this.tagService = tagService; }

    @GetMapping
    public ResponseEntity<List<TagResponse>> search(@RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) @Positive Long newsArticleId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(tagService.search(q, newsArticleId));
    }
}
