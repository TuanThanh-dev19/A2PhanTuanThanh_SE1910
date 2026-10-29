// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.a2phantuanthanh_se1910.entity.*;
import fpt.edu.a2phantuanthanh_se1910.repository.*;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NewsWriteApiTests {
    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) throws Exception {
        String database = System.getenv("SQLSERVER_M3_TEST_DATABASE");
        if (database == null || !database.matches("A2PhanTuanThanh_SE1910_M3Test_[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Set SQLSERVER_M3_TEST_DATABASE to a dedicated API test database");
        }
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new ResourcePropertySource("classpath:application.properties"));
        String url = environment.getRequiredProperty("spring.datasource.url").replace(
                "databaseName=A2PhanTuanThanh_SE1910;", "databaseName=" + database + "_Writes;");
        registry.add("spring.datasource.url", () -> url);
        registry.add("app.demo.enabled", () -> "false");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired SystemAccountRepository accounts;
    @Autowired CategoryRepository categories;
    @Autowired NewsArticleRepository articles;
    @Autowired TagRepository tags;
    @Autowired NewsTagRepository links;
    @Autowired PasswordEncoder passwords;
    private Category category;
    private SystemAccount staff;
    private String staffToken;
    private String adminToken;

    @BeforeEach
    void prepareData() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String password = "M6-test-" + suffix;
        String hash = passwords.encode(password);
        staff = accounts.saveAndFlush(new SystemAccount("Staff M6", suffix + "@staff.example.com", 2, hash));
        SystemAccount admin = accounts.saveAndFlush(new SystemAccount("Admin M6", suffix + "@admin.example.com", 1, hash));
        category = categories.saveAndFlush(new Category("Danh mục M6", null, null, 0));
        staffToken = login(staff.getAccountEmail(), password);
        adminToken = login(admin.getAccountEmail(), password);
    }

    @Test
    void createSavesFieldsTagsAndServerAuditForBothStatuses() throws Exception {
        for (int status : new int[]{0, 1}) {
            Tag free = tags.saveAndFlush(new Tag("Tag tự do", null));
            Map<String, Object> body = body(status);
            body.put("tags", List.of(tagInput(free.getTagId(), " Đã sửa tag ", " Ghi chú "), tagInput(null, "Tag mới", null)));
            body.put("createdById", Long.MAX_VALUE);
            body.put("updatedById", Long.MAX_VALUE);
            body.put("createdDate", "2000-01-01T00:00:00");
            body.put("modifiedDate", "2000-01-01T00:00:00");
            LocalDateTime before = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(1);
            JsonNode response = create(body);
            NewsArticle saved = articles.findById(response.path("newsArticleId").asLong()).orElseThrow();
            assertThat(saved.getNewsTitle()).isEqualTo("Bài viết M6");
            assertThat(saved.getHeadline()).isEqualTo("Headline M6");
            assertThat(saved.getNewsContent()).isEqualTo("Nội dung tiếng Việt\nDòng hai");
            assertThat(saved.getNewsSource()).isEqualTo("Nguồn M6");
            assertThat(response.path("createdById").asLong()).isEqualTo(staff.getAccountId());
            assertThat(saved.getCreatedDate()).isBetween(before, LocalDateTime.now(ZoneOffset.UTC));
            assertThat(saved.getUpdatedBy()).isNull();
            assertThat(saved.getModifiedDate()).isNull();
            assertThat(saved.getNewsStatus()).isEqualTo(status);
            assertThat(response.path("categoryId").asLong()).isEqualTo(category.getCategoryId());
            assertThat(response.path("tags").size()).isEqualTo(2);
            assertThat(links.findByNewsArticle_NewsArticleId(saved.getNewsArticleId())).hasSize(2);
            assertThat(tags.findById(free.getTagId()).orElseThrow().getTagName()).isEqualTo("Đã sửa tag");
            assertThat(response.toString()).doesNotContain("accountPassword", "$2a$");
        }
        create(body(1));
    }

    @Test
    void createRejectsInvalidFieldsTypesAndTagIdsWithoutSaving() throws Exception {
        long articleCount = articles.count();
        long tagCount = tags.count();
        Object[][] invalid = {{"newsTitle", " "}, {"headline", null}, {"newsContent", ""},
                {"newsTitle", "x".repeat(201)}, {"headline", "x".repeat(301)},
                {"newsContent", "x".repeat(20001)}, {"newsSource", "x".repeat(501)},
                {"categoryId", null}, {"categoryId", Long.MAX_VALUE}, {"categoryId", -1},
                {"newsStatus", 2}, {"newsStatus", 1.5}, {"newsStatus", "1"}, {"tags", null},
                {"tags", List.of(tagInput(null, " ", null))},
                {"tags", List.of(tagInput(null, "x".repeat(101), null))},
                {"tags", List.of(tagInput(null, "tag", "x".repeat(501)))},
                {"tags", List.of(tagInput(Long.MAX_VALUE, "tag", null))}};
        for (Object[] invalidField : invalid) {
            Map<String, Object> body = body(1);
            body.put((String) invalidField[0], invalidField[1]);
            mvc.perform(post("/api/news").header("Authorization", "Bearer " + staffToken)
                            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
        Tag free = tags.saveAndFlush(new Tag("Free", null));
        var duplicate = body(1);
        duplicate.put("tags", List.of(tagInput(free.getTagId(), "One", null), tagInput(free.getTagId(), "Two", null)));
        mvc.perform(post("/api/news").header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
        assertThat(articles.count()).isEqualTo(articleCount);
        assertThat(tags.count()).isEqualTo(tagCount + 1);
    }

    @Test
    void createRejectsForeignTagAndUnauthorizedUsersWithoutChangingData() throws Exception {
        Tag occupied = tags.saveAndFlush(new Tag("Occupied", "Keep note"));
        var first = body(1);
        first.put("tags", List.of(tagInput(occupied.getTagId(), "Occupied", "Keep note")));
        create(first);
        long articleCount = articles.count();
        long tagCount = tags.count();
        var second = body(1);
        second.put("tags", List.of(tagInput(null, "Must not persist", null), tagInput(occupied.getTagId(), "Wrong", null)));
        mvc.perform(post("/api/news").header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(second)))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/news").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body(1))))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/news").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body(1))))
                .andExpect(status().isForbidden());
        assertThat(articles.count()).isEqualTo(articleCount);
        assertThat(tags.count()).isEqualTo(tagCount);
        assertThat(tags.findById(occupied.getTagId()).orElseThrow().getTagName()).isEqualTo("Occupied");
    }

    private Map<String, Object> body(int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("newsTitle", " Bài viết M6 ");
        body.put("headline", " Headline M6 ");
        body.put("newsContent", " Nội dung tiếng Việt\nDòng hai ");
        body.put("newsSource", " Nguồn M6 ");
        body.put("categoryId", category.getCategoryId());
        body.put("newsStatus", status);
        body.put("tags", new ArrayList<>());
        return body;
    }

    private Map<String, Object> tagInput(Long id, String name, String note) {
        Map<String, Object> tag = new LinkedHashMap<>();
        tag.put("tagId", id);
        tag.put("tagName", name);
        tag.put("note", note);
        return tag;
    }

    private JsonNode create(Map<String, Object> body) throws Exception {
        String response = mvc.perform(post("/api/news").header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return json.readTree(response);
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).path("accessToken").asText();
    }
}
