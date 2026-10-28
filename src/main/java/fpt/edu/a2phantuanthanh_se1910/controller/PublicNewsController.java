// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.controller;

import fpt.edu.a2phantuanthanh_se1910.dto.response.PublicNewsDetail;
import fpt.edu.a2phantuanthanh_se1910.dto.response.PublicNewsSummary;
import fpt.edu.a2phantuanthanh_se1910.service.PublicNewsService;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/news")
public class PublicNewsController {
    private final PublicNewsService newsService;

    public PublicNewsController(PublicNewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<PublicNewsSummary> search(@RequestParam(defaultValue = "") String q) {
        return newsService.search(q);
    }

    @GetMapping("/{id}")
    public PublicNewsDetail detail(@PathVariable @Positive long id) {
        return newsService.findById(id);
    }
}
