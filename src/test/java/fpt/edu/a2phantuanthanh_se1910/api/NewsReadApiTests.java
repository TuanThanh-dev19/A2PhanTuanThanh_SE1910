// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NewsReadApiTests {
    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) throws Exception {
        String database = System.getenv("SQLSERVER_M3_TEST_DATABASE");
        if (database == null || !database.matches("A2PhanTuanThanh_SE1910_M3Test_[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Set SQLSERVER_M3_TEST_DATABASE to a dedicated API test database");
        }
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new ResourcePropertySource("classpath:application.properties"));
        String url = environment.getRequiredProperty("spring.datasource.url").replace(
                "databaseName=A2PhanTuanThanh_SE1910;", "databaseName=" + database + ";");
        registry.add("spring.datasource.url", () -> url);
        registry.add("app.demo.enabled", () -> "false");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired CategoryRepository categories;
    @Autowired SystemAccountRepository accounts;
    @Autowired NewsArticleRepository articles;
    @Autowired TagRepository tags;
    @Autowired NewsTagRepository links;
    @Autowired PasswordEncoder passwords;
    @Autowired EntityManager entityManager;

    private NewsArticle active;
    private NewsArticle inactive;
    private NewsArticle older;
    private Tag firstTag;
    private Tag secondTag;
    private String staffToken;
    private String adminToken;

    @BeforeEach
    void prepareData() throws Exception {
        String password = "News-read-" + java.util.UUID.randomUUID();
        String hash = passwords.encode(password);
        SystemAccount staff = accounts.saveAndFlush(new SystemAccount(
                "Staff đọc tin", "staff.news@example.com", 2, hash));
        SystemAccount otherStaff = accounts.saveAndFlush(new SystemAccount(
                "Staff khác", "other.news@example.com", 2, hash));
        accounts.saveAndFlush(new SystemAccount("Admin tin", "admin.news@example.com", 1, hash));
        Category category = categories.saveAndFlush(new Category("Danh mục ẩn", null, null, 0));
        LocalDateTime created = LocalDateTime.of(2026, 10, 29, 2, 0);
        active = articles.saveAndFlush(new NewsArticle("Tin công khai 100%_ [M6]!", "Tiêu đề phụ riêng",
                created, "Nội dung tiếng Việt", "Nguồn trường", category, 1, staff));
        inactive = new NewsArticle("Tin nội bộ", "Headline hidden", created,
                "Nội dung content-hidden", null, category, 0, otherStaff);
        inactive.setUpdatedBy(staff);
        inactive.setModifiedDate(created.plusHours(1));
        inactive = articles.saveAndFlush(inactive);
        older = articles.saveAndFlush(new NewsArticle("Tin cũ", "Thông báo cũ", created.minusDays(1),
                "Nội dung cũ", null, category, 1, staff));
        firstTag = tags.saveAndFlush(new Tag("Thẻ một", "Ghi chú tiếng Việt"));
        secondTag = tags.saveAndFlush(new Tag("Thẻ hai", null));
        links.saveAndFlush(new NewsTag(inactive, secondTag));
        links.saveAndFlush(new NewsTag(inactive, firstTag));
        staffToken = login("staff.news@example.com", password);
        adminToken = login("admin.news@example.com", password);
        entityManager.clear();
    }

    @Test
    void listIncludesBothStatusesAndOtherCreatorsWithStableOrderAndSafeFields() throws Exception {
        JsonNode response = read("/api/news", "");
        assertThat(response.size()).isEqualTo(3);
        NewsArticle[] expected = {inactive, active, older};
        for (int index = 0; index < expected.length; index++) {
            NewsArticle saved = articles.findById(expected[index].getNewsArticleId()).orElseThrow();
            JsonNode row = response.get(index);
            assertThat(row.size()).isEqualTo(10);
            assertThat(row.path("newsArticleId").asLong()).isEqualTo(saved.getNewsArticleId());
            assertThat(row.path("newsTitle").asText()).isEqualTo(saved.getNewsTitle());
            assertThat(row.path("headline").asText()).isEqualTo(saved.getHeadline());
            assertThat(row.path("createdDate")).isEqualTo(json.valueToTree(saved.getCreatedDate()));
            assertThat(row.path("categoryId").asLong()).isEqualTo(saved.getCategory().getCategoryId());
            assertThat(row.path("categoryName").asText()).isEqualTo(saved.getCategory().getCategoryName());
            assertThat(row.path("newsStatus").asInt()).isEqualTo(saved.getNewsStatus());
            assertThat(row.path("createdById").asLong()).isEqualTo(saved.getCreatedBy().getAccountId());
            assertThat(row.path("createdByName").asText()).isEqualTo(saved.getCreatedBy().getAccountName());
            assertThat(row.path("modifiedDate")).isEqualTo(json.valueToTree(saved.getModifiedDate()));
        }
        assertThat(response.toString()).doesNotContain("Password", "password", "$2a$", "accountEmail");
    }

    @Test
    void detailIncludesInactiveContentAuditAndTagsWhilePublicStillHidesIt() throws Exception {
        JsonNode response = read("/api/news/" + inactive.getNewsArticleId(), "");
        NewsArticle saved = articles.findById(inactive.getNewsArticleId()).orElseThrow();
        assertThat(response.size()).isEqualTo(15);
        assertThat(response.path("newsArticleId").asLong()).isEqualTo(saved.getNewsArticleId());
        assertThat(response.path("newsTitle").asText()).isEqualTo(saved.getNewsTitle());
        assertThat(response.path("headline").asText()).isEqualTo(saved.getHeadline());
        assertThat(response.path("createdDate")).isEqualTo(json.valueToTree(saved.getCreatedDate()));
        assertThat(response.path("newsContent").asText()).isEqualTo(saved.getNewsContent());
        assertThat(response.path("newsSource").isNull()).isTrue();
        assertThat(response.path("categoryId").asLong()).isEqualTo(saved.getCategory().getCategoryId());
        assertThat(response.path("categoryName").asText()).isEqualTo(saved.getCategory().getCategoryName());
        assertThat(response.path("newsStatus").asInt()).isZero();
        assertThat(response.path("createdById").asLong()).isEqualTo(saved.getCreatedBy().getAccountId());
        assertThat(response.path("createdByName").asText()).isEqualTo(saved.getCreatedBy().getAccountName());
        assertThat(response.path("updatedById").asLong()).isEqualTo(saved.getUpdatedBy().getAccountId());
        assertThat(response.path("updatedByName").asText()).isEqualTo(saved.getUpdatedBy().getAccountName());
        assertThat(response.path("modifiedDate")).isEqualTo(json.valueToTree(saved.getModifiedDate()));
        assertThat(response.path("tags").size()).isEqualTo(2);
        Tag[] expectedTags = {firstTag, secondTag};
        for (int index = 0; index < expectedTags.length; index++) {
            Tag tag = tags.findById(expectedTags[index].getTagId()).orElseThrow();
            JsonNode item = response.path("tags").get(index);
            assertThat(item.size()).isEqualTo(3);
            assertThat(item.path("tagId").asLong()).isEqualTo(tag.getTagId());
            assertThat(item.path("tagName").asText()).isEqualTo(tag.getTagName());
            assertThat(item.path("note")).isEqualTo(json.valueToTree(tag.getNote()));
        }
        assertThat(response.toString()).doesNotContain("Password", "password", "$2a$", "accountEmail");
        JsonNode unedited = read("/api/news/" + active.getNewsArticleId(), "");
        assertThat(unedited.path("updatedById").isNull()).isTrue();
        assertThat(unedited.path("updatedByName").isNull()).isTrue();
        assertThat(unedited.path("modifiedDate").isNull()).isTrue();
        assertThat(unedited.path("newsSource").asText()).isEqualTo(active.getNewsSource());
        assertThat(unedited.path("tags").isEmpty()).isTrue();
        mvc.perform(get("/api/public/news/" + inactive.getNewsArticleId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/public/news")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void searchMatchesTitleHeadlineContentAndLiteralWildcardsWithoutChangingData() throws Exception {
        for (String keyword : new String[]{"  TIN CÔNG KHAI  ", "TIÊU ĐỀ PHỤ RIÊNG", "TIẾNG VIỆT",
                "%", "_", "[", "!", "100%_ [M6]!"}) {
            JsonNode response = read("/api/news", keyword);
            assertThat(response.size()).isEqualTo(1);
            assertThat(response.get(0).path("newsArticleId").asLong()).isEqualTo(active.getNewsArticleId());
        }
        for (String keyword : new String[]{"TIN NỘI BỘ", "HEADLINE HIDDEN", "CONTENT-HIDDEN"}) {
            JsonNode response = read("/api/news", keyword);
            assertThat(response.size()).isEqualTo(1);
            assertThat(response.get(0).path("newsArticleId").asLong()).isEqualTo(inactive.getNewsArticleId());
        }
        assertThat(read("/api/news", "   ").size()).isEqualTo(3);
        assertThat(read("/api/news", "no-match-M6").isEmpty()).isTrue();
        assertThat(read("/api/news", "x".repeat(200)).isEmpty()).isTrue();
        mvc.perform(get("/api/news").param("q", "x".repeat(201))
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("code").value("VALIDATION_ERROR"));
        entityManager.clear();
        assertThat(articles.count()).isEqualTo(3);
        assertThat(links.count()).isEqualTo(2);
        NewsArticle saved = articles.findById(active.getNewsArticleId()).orElseThrow();
        assertThat(saved.getNewsTitle()).isEqualTo(active.getNewsTitle());
        assertThat(saved.getModifiedDate()).isNull();
    }

    @Test
    void readsOnlyAllowStaffAndRejectInvalidOrMissingIds() throws Exception {
        for (String path : new String[]{"/api/news", "/api/news/" + inactive.getNewsArticleId()}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
            mvc.perform(get(path).header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }
        for (String id : new String[]{"0", "-1", "abc", "1.5", "9223372036854775808"}) {
            mvc.perform(get("/api/news/" + id).header("Authorization", "Bearer " + staffToken))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("code").value("VALIDATION_ERROR"));
        }
        mvc.perform(get("/api/news/" + Long.MAX_VALUE).header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound()).andExpect(jsonPath("code").value("NOT_FOUND"));
    }

    private JsonNode read(String path, String query) throws Exception {
        String body = mvc.perform(get(path).param("q", query).header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return json.readTree(body);
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).path("accessToken").asText();
    }
}
