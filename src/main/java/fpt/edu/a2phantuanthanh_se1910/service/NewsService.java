// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.response.NewsDetail;
import fpt.edu.a2phantuanthanh_se1910.dto.response.NewsSummary;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsArticle;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsTag;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.entity.Tag;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsArticleRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsTagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class NewsService {
    private final NewsArticleRepository articles;
    private final NewsTagRepository newsTags;

    public NewsService(NewsArticleRepository articles, NewsTagRepository newsTags) {
        this.articles = articles;
        this.newsTags = newsTags;
    }

    public List<NewsSummary> search(String query) {
        String keyword = query.trim();
        if (keyword.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search must be at most 200 characters");
        }
        // Treat SQL Server LIKE wildcards as literal search text.
        keyword = keyword.toLowerCase(Locale.ROOT).replace("!", "!!")
                .replace("%", "!%").replace("_", "!_").replace("[", "![");
        List<NewsSummary> response = new ArrayList<>();
        for (NewsArticle article : articles.searchNews(keyword)) {
            response.add(new NewsSummary(article.getNewsArticleId(), article.getNewsTitle(),
                    article.getHeadline(), article.getCreatedDate(), article.getCategory().getCategoryId(),
                    article.getCategory().getCategoryName(), article.getNewsStatus(),
                    article.getCreatedBy().getAccountId(), article.getCreatedBy().getAccountName(),
                    article.getModifiedDate()));
        }
        return response;
    }

    public NewsDetail findById(long id) {
        NewsArticle article = articles.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News article not found"));
        Long updatedById = null;
        String updatedByName = null;
        SystemAccount updater = article.getUpdatedBy();
        if (updater != null) {
            updatedById = updater.getAccountId();
            updatedByName = updater.getAccountName();
        }
        List<NewsDetail.TagResponse> tags = new ArrayList<>();
        for (NewsTag link : newsTags.findByNewsArticle_NewsArticleId(id)) {
            Tag tag = link.getTag();
            tags.add(new NewsDetail.TagResponse(tag.getTagId(), tag.getTagName(), tag.getNote()));
        }
        tags.sort(Comparator.comparing(NewsDetail.TagResponse::tagId));
        return new NewsDetail(article.getNewsArticleId(), article.getNewsTitle(), article.getHeadline(),
                article.getCreatedDate(), article.getNewsContent(), article.getNewsSource(),
                article.getCategory().getCategoryId(), article.getCategory().getCategoryName(),
                article.getNewsStatus(), article.getCreatedBy().getAccountId(),
                article.getCreatedBy().getAccountName(), updatedById, updatedByName,
                article.getModifiedDate(), tags);
    }
}
