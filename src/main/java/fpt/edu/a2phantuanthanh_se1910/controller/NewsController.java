// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.controller;

import fpt.edu.a2phantuanthanh_se1910.dto.response.NewsDetail;
import fpt.edu.a2phantuanthanh_se1910.dto.response.NewsSummary;
import fpt.edu.a2phantuanthanh_se1910.service.NewsService;
import fpt.edu.a2phantuanthanh_se1910.dto.request.NewsRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.security.Principal;
import java.net.URI;
import jakarta.validation.constraints.Positive;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
public class NewsController {
    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public ResponseEntity<List<NewsSummary>> search(@RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(newsService.search(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsDetail> findById(@PathVariable @Positive long id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(newsService.findById(id));
    }

    @PostMapping
    public ResponseEntity<NewsDetail> create(@Valid @RequestBody NewsRequest request, Principal principal) {
        NewsDetail response = newsService.create(request, Long.parseLong(principal.getName()));
        return ResponseEntity.created(URI.create("/api/news/" + response.newsArticleId()))
                .cacheControl(CacheControl.noStore()).body(response);
    }
}
