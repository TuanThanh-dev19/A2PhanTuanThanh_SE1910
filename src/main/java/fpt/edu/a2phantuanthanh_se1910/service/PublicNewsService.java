// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.response.PublicNewsDetail;
import fpt.edu.a2phantuanthanh_se1910.dto.response.PublicNewsSummary;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsArticle;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsTag;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsArticleRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsTagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class PublicNewsService {
    private final NewsArticleRepository articles;
    private final NewsTagRepository newsTags;

    public PublicNewsService(NewsArticleRepository articles, NewsTagRepository newsTags) {
        this.articles = articles;
        this.newsTags = newsTags;
    }

    public List<PublicNewsSummary> search(String query) {
        String keyword = query.trim();
        if (keyword.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search must be at most 200 characters");
        }
        // Escape LIKE wildcards so q is a literal keyword, not a SQL search pattern.
        keyword = keyword.toLowerCase(Locale.ROOT).replace("!", "!!")
                .replace("%", "!%").replace("_", "!_").replace("[", "![");
        List<NewsArticle> matches = articles.searchActiveNews(keyword);
        List<PublicNewsSummary> response = new ArrayList<>();
        for (NewsArticle article : matches) {
            response.add(new PublicNewsSummary(article.getNewsArticleId(), article.getNewsTitle(),
                    article.getHeadline(), article.getCreatedDate(), article.getCategory().getCategoryId(),
                    article.getCategory().getCategoryName()));
        }
        return response;
    }

    public PublicNewsDetail findById(long id) {
        NewsArticle article = articles.findByNewsArticleIdAndNewsStatus(id, 1)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News article not found"));
        List<PublicNewsDetail.TagResponse> tags = new ArrayList<>();
        for (NewsTag link : newsTags.findByNewsArticle_NewsArticleId(id)) {
            tags.add(new PublicNewsDetail.TagResponse(link.getTag().getTagId(), link.getTag().getTagName()));
        }
        return new PublicNewsDetail(article.getNewsArticleId(), article.getNewsTitle(), article.getHeadline(),
                article.getCreatedDate(), article.getNewsContent(), article.getNewsSource(),
                article.getCategory().getCategoryId(), article.getCategory().getCategoryName(),
                article.getCreatedBy().getAccountName(), tags);
    }
}
