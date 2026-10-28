// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.a2phantuanthanh_se1910.entity.Category;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.repository.CategoryRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
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

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryApiTests {
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
    @Autowired PasswordEncoder passwords;

    private Category parent;
    private String staffToken;
    private String adminToken;

    @BeforeEach
    void prepareData() throws Exception {
        String password = "Category-test-" + java.util.UUID.randomUUID();
        String hash = passwords.encode(password);
        accounts.saveAndFlush(new SystemAccount("Staff Category", "staff.category@example.com", 2, hash));
        accounts.saveAndFlush(new SystemAccount("Admin Category", "admin.category@example.com", 1, hash));
        parent = categories.saveAndFlush(new Category("Parent Category", null, null, 0));
        staffToken = login("staff.category@example.com", password);
        adminToken = login("admin.category@example.com", password);
    }

    @Test
    void createTrimsFieldsAndSavesOptionalParentAndBothStatuses() throws Exception {
        for (Long parentId : new Long[]{null, parent.getCategoryId()}) {
            for (int status : new int[]{0, 1}) {
                Map<String, Object> body = categoryBody("  Danh mục mới  ", parentId, status);
                body.put("categoryDescription", "  Mô tả tiếng Việt  ");
                JsonNode response = create(body);
                assertThat(response.size()).isEqualTo(6);
                Category saved = categories.findById(response.path("categoryId").asLong()).orElseThrow();
                assertThat(saved.getCategoryName()).isEqualTo("Danh mục mới");
                assertThat(saved.getCategoryDescription()).isEqualTo("Mô tả tiếng Việt");
                assertThat(saved.getIsActive()).isEqualTo(status);
                if (parentId != null) {
                    assertThat(response.path("parentCategoryId").asLong()).isEqualTo(parentId);
                    assertThat(saved.getParentCategory().getCategoryId()).isEqualTo(parentId);
                    assertThat(response.path("parentCategoryName").asText()).isEqualTo(parent.getCategoryName());
                } else {
                    assertThat(response.path("parentCategoryId").isNull()).isTrue();
                    assertThat(saved.getParentCategory()).isNull();
                }
            }
        }
        var boundary = categoryBody("x".repeat(100), null, 1);
        boundary.put("categoryDescription", "x".repeat(1000));
        create(boundary);
        create(categoryBody("Không mô tả", null, 1));
    }

    @Test
    void createRejectsInvalidFieldsTypesAndMissingParentWithoutSaving() throws Exception {
        mvc.perform(post("/api/categories").header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("fieldErrors.categoryName").exists())
                .andExpect(jsonPath("fieldErrors.isActive").exists());
        Object[][] invalidValues = {{"categoryName", " "}, {"categoryName", null}, {"categoryName", "x".repeat(101)},
                {"categoryDescription", "x".repeat(1001)}, {"parentCategoryId", 0}, {"parentCategoryId", -1},
                {"parentCategoryId", 1.5}, {"parentCategoryId", "1"}, {"parentCategoryId", Long.MAX_VALUE},
                {"isActive", null}, {"isActive", -1}, {"isActive", 2}, {"isActive", 0.5}, {"isActive", "1"}};
        for (Object[] invalid : invalidValues) {
            var body = categoryBody("Invalid category", null, 1);
            body.put((String) invalid[0], invalid[1]);
            mvc.perform(post("/api/categories").header("Authorization", "Bearer " + staffToken)
                            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
        assertThat(categories.count()).isEqualTo(1);
    }

    @Test
    void createOnlyAllowsStaff() throws Exception {
        String body = json.writeValueAsString(categoryBody("Unauthorized", null, 1));
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/categories").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        assertThat(categories.count()).isEqualTo(1);
    }

    private JsonNode create(Map<String, Object> body) throws Exception {
        String response = mvc.perform(post("/api/categories").header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response);
    }

    private Map<String, Object> categoryBody(String name, Long parentId, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("categoryName", name);
        body.put("categoryDescription", null);
        body.put("parentCategoryId", parentId);
        body.put("isActive", status);
        return body;
    }

    private String login(String email, String password) throws Exception {
        String response = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(response).path("accessToken").asText();
    }
}
