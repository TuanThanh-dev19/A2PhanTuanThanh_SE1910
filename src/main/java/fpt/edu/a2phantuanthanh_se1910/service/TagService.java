// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.response.TagResponse;
import fpt.edu.a2phantuanthanh_se1910.entity.Tag;
import fpt.edu.a2phantuanthanh_se1910.repository.TagRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsArticleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class TagService {
    private final TagRepository tags;
    private final NewsArticleRepository articles;

    public TagService(TagRepository tags, NewsArticleRepository articles) {
        this.tags = tags;
        this.articles = articles;
    }

    public List<TagResponse> search(String query, Long articleId) {
        String keyword = query.trim();
        if (keyword.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search must be at most 200 characters");
        }
        if (articleId != null && !articles.existsById(articleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "News article not found");
        }
        keyword = keyword.toLowerCase(Locale.ROOT).replace("!", "!!")
                .replace("%", "!%").replace("_", "!_").replace("[", "![");
        List<TagResponse> response = new ArrayList<>();
        for (Tag tag : tags.searchAvailable(keyword, articleId)) {
            response.add(new TagResponse(tag.getTagId(), tag.getTagName(), tag.getNote()));
        }
        return response;
    }
}
