// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.persistence;

import fpt.edu.a2phantuanthanh_se1910.entity.Category;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsArticle;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsTag;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.entity.Tag;
import fpt.edu.a2phantuanthanh_se1910.repository.CategoryRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsArticleRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsTagRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.TagRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/** Test-only data: never registered as an application startup component. */
final class PersistenceFixture {

    static final String TITLE = "Ngày hội sáng tạo tại Đại học FPT";
    static final String CONTENT = "Sinh viên nghiên cứu trí tuệ nhân tạo, bảo vệ môi trường. ".repeat(100);
    static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 10, 28, 10, 0);

    private final SystemAccountRepository accounts;
    private final CategoryRepository categories;
    private final NewsArticleRepository articles;
    private final TagRepository tags;
    private final NewsTagRepository links;

    PersistenceFixture(SystemAccountRepository accounts, CategoryRepository categories,
                       NewsArticleRepository articles, TagRepository tags, NewsTagRepository links) {
        this.accounts = accounts;
        this.categories = categories;
        this.articles = articles;
        this.tags = tags;
        this.links = links;
    }

    FixtureIds create() {
        if (accounts.count() != 0 || categories.count() != 0 || articles.count() != 0
                || tags.count() != 0 || links.count() != 0) {
            throw new IllegalStateException("Fixture requires an empty, isolated M2 test database");
        }

        // Opaque random test values; authentication/password hashing belongs to M3.
        SystemAccount admin = accounts.saveAndFlush(new SystemAccount(
                "Quản trị thử nghiệm", "admin@example.invalid", 1, UUID.randomUUID().toString()));
        SystemAccount creator = accounts.saveAndFlush(new SystemAccount(
                "Nguyễn Thị Ánh", "anh@example.invalid", 2, UUID.randomUUID().toString()));
        SystemAccount updater = accounts.saveAndFlush(new SystemAccount(
                "Trần Tuấn Đạt", "dat@example.invalid", 2, UUID.randomUUID().toString()));

        Category parent = categories.saveAndFlush(new Category("Giáo dục", "Tin đại học", null, 1));
        Category child = categories.saveAndFlush(new Category("Đời sống sinh viên", "Hoạt động cộng đồng", parent, 0));
        Category unused = categories.saveAndFlush(new Category("Chuyên mục chưa có bài", null, null, 1));

        NewsArticle active = articles.saveAndFlush(new NewsArticle(TITLE, "Khám phá ý tưởng mới",
                CREATED_AT, CONTENT, "Bản tin Đại học FPT", child, 1, creator));
        active.setUpdatedBy(updater);
        active.setModifiedDate(CREATED_AT.plusHours(1));
        articles.saveAndFlush(active);
        NewsArticle inactive = articles.saveAndFlush(new NewsArticle("Bài nháp tiếng Việt", "Chưa công bố",
                CREATED_AT.plusMinutes(1), "Nội dung bài nháp", null, parent, 0, creator));
        NewsArticle otherCreator = articles.saveAndFlush(new NewsArticle("Bài của tác giả khác", "Tin mới",
                CREATED_AT.plusMinutes(2), "Một nội dung khác", null, parent, 1, updater));

        Tag linked = tags.saveAndFlush(new Tag("Sáng tạo", "Nhãn tiếng Việt"));
        Tag secondLinked = tags.saveAndFlush(new Tag("Sinh viên", null));
        Tag free = tags.saveAndFlush(new Tag("Chưa sử dụng", null));
        links.saveAndFlush(new NewsTag(active, linked));
        links.saveAndFlush(new NewsTag(active, secondLinked));

        return new FixtureIds(admin.getAccountId(), creator.getAccountId(), updater.getAccountId(),
                parent.getCategoryId(), child.getCategoryId(), unused.getCategoryId(),
                active.getNewsArticleId(), inactive.getNewsArticleId(), otherCreator.getNewsArticleId(),
                linked.getTagId(), secondLinked.getTagId(), free.getTagId());
    }

    record FixtureIds(Long admin, Long creator, Long updater, Long parent, Long child, Long unusedCategory,
                      Long active, Long inactive, Long otherCreator, Long linkedTag, Long secondTag, Long freeTag) {
    }
}
