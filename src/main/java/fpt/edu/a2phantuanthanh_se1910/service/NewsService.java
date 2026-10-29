// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.response.NewsDetail;
import fpt.edu.a2phantuanthanh_se1910.dto.response.NewsSummary;
import fpt.edu.a2phantuanthanh_se1910.dto.request.NewsRequest;
import fpt.edu.a2phantuanthanh_se1910.entity.Category;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsArticle;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsTag;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.entity.Tag;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsArticleRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsTagRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.CategoryRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class NewsService {
    private final NewsArticleRepository articles;
    private final NewsTagRepository newsTags;
    private final CategoryRepository categories;
    private final SystemAccountRepository accounts;
    private final TagRepository tagRepository;

    public NewsService(NewsArticleRepository articles, NewsTagRepository newsTags,
                       CategoryRepository categories, SystemAccountRepository accounts, TagRepository tagRepository) {
        this.articles = articles;
        this.newsTags = newsTags;
        this.categories = categories;
        this.accounts = accounts;
        this.tagRepository = tagRepository;
    }

    public List<NewsSummary> search(String query) {
        return mapSummaries(articles.searchNews(searchKeyword(query)));
    }

    public List<NewsSummary> searchHistory(String query, long accountId) {
        return mapSummaries(articles.searchCreatedNews(accountId, searchKeyword(query)));
    }

    private String searchKeyword(String query) {
        String keyword = query.trim();
        if (keyword.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search must be at most 200 characters");
        }
        // Treat SQL Server LIKE wildcards as literal search text.
        return keyword.toLowerCase(Locale.ROOT).replace("!", "!!")
                .replace("%", "!%").replace("_", "!_").replace("[", "![");
    }

    private List<NewsSummary> mapSummaries(List<NewsArticle> matches) {
        List<NewsSummary> response = new ArrayList<>();
        for (NewsArticle article : matches) {
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

    @Transactional
    public NewsDetail create(NewsRequest request, long accountId) {
        Category category = requireCategory(request.categoryId());
        SystemAccount creator = accounts.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
        Map<Long, Tag> existingTags = validateTags(request.tags(), null);
        LocalDateTime createdDate = LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
        NewsArticle article = new NewsArticle(request.newsTitle(), request.headline(), createdDate,
                request.newsContent(), request.newsSource(), category, request.newsStatus(), creator);
        articles.saveAndFlush(article);
        saveTags(article, request.tags(), existingTags);
        return findById(article.getNewsArticleId());
    }

    private Category requireCategory(long id) {
        return categories.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "The category no longer exists. Please select another category."));
    }

    @Transactional
    public void delete(long id) {
        NewsArticle article = articles.findForUpdate(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "News article not found"));
        newsTags.deleteAll(newsTags.findByNewsArticle_NewsArticleId(id));
        newsTags.flush();
        articles.delete(article);
        articles.flush();
    }

    @Transactional
    public NewsDetail update(long id, NewsRequest request, long accountId) {
        NewsArticle article = articles.findForUpdate(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "News article not found"));
        Category category = requireCategory(request.categoryId());
        SystemAccount updater = accounts.findById(accountId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account no longer exists"));
        Map<Long, Tag> existingTags = validateTags(request.tags(), id);
        for (NewsTag link : newsTags.findByNewsArticle_NewsArticleId(id)) {
            if (!existingTags.containsKey(link.getTag().getTagId())) {
                newsTags.delete(link);
            }
        }
        newsTags.flush();
        article.setNewsTitle(request.newsTitle());
        article.setHeadline(request.headline());
        article.setNewsContent(request.newsContent());
        article.setNewsSource(request.newsSource());
        article.setCategory(category);
        article.setNewsStatus(request.newsStatus());
        article.setUpdatedBy(updater);
        article.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS));
        articles.saveAndFlush(article);
        saveTags(article, request.tags(), existingTags);
        return findById(id);
    }

    private Map<Long, Tag> validateTags(List<NewsRequest.TagInput> inputs, Long articleId) {
        Set<Long> tagIds = new TreeSet<>();
        for (NewsRequest.TagInput input : inputs) {
            if (input.tagId() != null && !tagIds.add(input.tagId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag IDs must not be repeated.");
            }
        }
        Map<Long, Tag> existingTags = new HashMap<>();
        // Lock existing tags in ID order so two requests cannot claim the same free tag.
        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findForUpdate(tagId).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "The tag no longer exists. Please select another tag."));
            NewsTag link = newsTags.findByTag_TagId(tagId).orElse(null);
            if (link != null && !link.getNewsArticle().getNewsArticleId().equals(articleId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The tag already belongs to another article.");
            }
            existingTags.put(tagId, tag);
        }
        return existingTags;
    }

    private void saveTags(NewsArticle article, List<NewsRequest.TagInput> inputs, Map<Long, Tag> existingTags) {
        for (NewsRequest.TagInput input : inputs) {
            Tag tag;
            if (input.tagId() == null) {
                tag = new Tag(input.tagName(), input.note());
            } else {
                tag = existingTags.get(input.tagId());
                tag.setTagName(input.tagName());
                tag.setNote(input.note());
            }
            tagRepository.saveAndFlush(tag);
            if (!newsTags.existsByTag_TagId(tag.getTagId())) {
                newsTags.save(new NewsTag(article, tag));
            }
        }
        newsTags.flush();
    }
}
