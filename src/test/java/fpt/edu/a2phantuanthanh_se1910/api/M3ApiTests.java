// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fpt.edu.a2phantuanthanh_se1910.bootstrap.DemoDataInitializer;
import fpt.edu.a2phantuanthanh_se1910.entity.*;
import fpt.edu.a2phantuanthanh_se1910.repository.*;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class M3ApiTests {
    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) throws Exception {
        String database = System.getenv("SQLSERVER_M3_TEST_DATABASE");
        if (database == null || !database.matches("A2PhanTuanThanh_SE1910_M3Test_[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Set SQLSERVER_M3_TEST_DATABASE to a dedicated M3 test database");
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
    @Autowired SystemAccountRepository accounts;
    @Autowired CategoryRepository categories;
    @Autowired NewsArticleRepository articles;
    @Autowired TagRepository tags;
    @Autowired NewsTagRepository links;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtEncoder encoder;
    @Autowired JwtDecoder decoder;
    @Autowired Validator validator;
    @Autowired JdbcTemplate jdbc;

    private final String password = "M3-test-only-" + java.util.UUID.randomUUID();
    private SystemAccount admin;
    private SystemAccount staff;
    private NewsArticle active;
    private NewsArticle inactive;

    @BeforeEach
    void prepareData() {
        String hash = passwords.encode(password);
        admin = accounts.saveAndFlush(new SystemAccount("Admin test", "admin.test@example.com", 1, hash));
        staff = accounts.saveAndFlush(new SystemAccount("Staff test", "staff.test@example.com", 2, hash));
        Category category = categories.saveAndFlush(new Category("Danh mục ẩn", "M3 test", null, 0));
        active = articles.saveAndFlush(new NewsArticle("Tin M3 100%_ [public]", "Headline unique-HEAD",
                LocalDateTime.of(2026, 10, 28, 1, 0), "Nội dung Việt unique-CONTENT", null, category, 1, staff));
        inactive = articles.saveAndFlush(new NewsArticle("Tin M3 nội bộ", "Không công khai",
                LocalDateTime.of(2026, 10, 28, 2, 0), "unique-hidden", null, category, 0, staff));
        Tag tag = tags.saveAndFlush(new Tag("M3", "test"));
        links.saveAndFlush(new NewsTag(active, tag));
    }

    @Test
    void loginReturnsTokenAndSafeAccountForAdminAndStaff() throws Exception {
        for (SystemAccount account : new SystemAccount[]{admin, staff}) {
            JsonNode response = login(account.getAccountEmail().toUpperCase(), password);
            assertThat(response.path("account").path("accountId").asLong()).isEqualTo(account.getAccountId());
            assertThat(response.path("account").path("accountRole").asInt()).isEqualTo(account.getAccountRole());
            assertThat(response.toString()).doesNotContain("password", "Password", "$2a$");
            Jwt token = decoder.decode(response.path("accessToken").asText());
            assertThat(token.getSubject()).isEqualTo(account.getAccountId().toString());
            assertThat(token.getExpiresAt()).isAfter(Instant.now());
            assertThat(token.getClaims()).doesNotContainKeys("password", "email");
        }
    }

    @Test
    void wrongEmailAndPasswordReturnSame401() throws Exception {
        String first = mvc.perform(loginRequest(admin.getAccountEmail(), "incorrect"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("code").value("UNAUTHORIZED"))
                .andReturn().getResponse().getContentAsString();
        String second = mvc.perform(loginRequest("unknown@example.com", password))
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(first).path("message")).isEqualTo(json.readTree(second).path("message"));
    }

    @Test
    void invalidPayloadAndOversizedUtf8PasswordReturn400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("fieldErrors.email").exists())
                .andExpect(jsonPath("fieldErrors.password").exists());
        mvc.perform(loginRequest("not-email", password)).andExpect(status().isBadRequest());
        mvc.perform(loginRequest(admin.getAccountEmail(), " ")).andExpect(status().isBadRequest());
        mvc.perform(loginRequest(admin.getAccountEmail(), "é".repeat(37))).andExpect(status().isBadRequest());
        mvc.perform(loginRequest(admin.getAccountEmail(), "x".repeat(73))).andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingMalformedExpiredWrongSignatureAndIssuerTokensReturn401() throws Exception {
        mvc.perform(get("/api/accounts")).andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));
        String valid = login(admin.getAccountEmail(), password).path("accessToken").asText();
        String[] tokens = {"bad-token", valid.substring(0, valid.lastIndexOf('.') + 1) + "A".repeat(43),
                token(admin, Instant.now().minusSeconds(2), "A2PhanTuanThanh_SE1910"),
                token(admin, Instant.now().plusSeconds(60), "wrong-issuer")};
        for (String token : tokens) {
            mvc.perform(get("/api/accounts").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized()).andExpect(jsonPath("status").value(401));
        }
    }

    @Test
    void roleRulesProtectReadsAndWrites() throws Exception {
        String adminToken = login(admin.getAccountEmail(), password).path("accessToken").asText();
        String staffToken = login(staff.getAccountEmail(), password).path("accessToken").asText();
        String[] staffRoutes = {"/api/categories", "/api/categories/" + active.getCategory().getCategoryId(),
                "/api/news", "/api/news/" + active.getNewsArticleId(),
                "/api/tags", "/api/profile", "/api/profile/news"};
        for (String route : staffRoutes) {
            for (String method : new String[]{"GET", "POST", "PUT", "DELETE"}) {
                mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), route)
                                .header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isForbidden()).andExpect(jsonPath("code").value("FORBIDDEN"));
                mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), route))
                        .andExpect(status().isUnauthorized());
            }
            if (route.startsWith("/api/categories") || route.startsWith("/api/news")
                    || route.equals("/api/tags") || route.equals("/api/profile") || route.equals("/api/profile/news")) {
                mvc.perform(get(route).header("Authorization", "Bearer " + staffToken)).andExpect(status().isOk());
            } else {
                // Modules without a controller still return 404 after passing the security filter.
                mvc.perform(get(route).header("Authorization", "Bearer " + staffToken)).andExpect(status().isNotFound());
            }
        }
        for (String route : new String[]{"/api/accounts", "/api/accounts/9223372036854775807"}) {
            for (String method : new String[]{"GET", "POST", "PUT", "DELETE"}) {
                mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), route)
                                .header("Authorization", "Bearer " + staffToken)).andExpect(status().isForbidden());
                mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), route))
                        .andExpect(status().isUnauthorized());
            }
            if (route.equals("/api/accounts")) {
                mvc.perform(get(route).header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk());
            } else {
                mvc.perform(get(route).header("Authorization", "Bearer " + adminToken)).andExpect(status().isNotFound());
            }
        }
    }

    @Test
    void categoryListAndDetailMatchDatabaseWithFlatParentAndBothStatuses() throws Exception {
        Category parent = active.getCategory();
        categories.saveAndFlush(new Category("Danh mục con", null, parent, 1));
        String token = login(staff.getAccountEmail(), password).path("accessToken").asText();
        String body = mvc.perform(get("/api/categories").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsString();
        JsonNode response = json.readTree(body);
        var records = categories.findAll(org.springframework.data.domain.Sort.by("categoryId"));
        assertThat(response.size()).isEqualTo(2);
        for (int index = 0; index < records.size(); index++) {
            Category category = records.get(index);
            JsonNode row = response.get(index);
            assertThat(row.size()).isEqualTo(6);
            assertThat(row.path("categoryId").asLong()).isEqualTo(category.getCategoryId());
            assertThat(row.path("categoryName").asText()).isEqualTo(category.getCategoryName());
            assertThat(row.get("categoryDescription")).isEqualTo(json.valueToTree(category.getCategoryDescription()));
            assertThat(row.path("isActive").asInt()).isEqualTo(category.getIsActive());
            if (category.getParentCategory() == null) {
                assertThat(row.path("parentCategoryId").isNull()).isTrue();
                assertThat(row.path("parentCategoryName").isNull()).isTrue();
            } else {
                assertThat(row.path("parentCategoryId").asLong()).isEqualTo(parent.getCategoryId());
                assertThat(row.path("parentCategoryName").asText()).isEqualTo(parent.getCategoryName());
            }
            String detail = mvc.perform(get("/api/categories/" + category.getCategoryId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            assertThat(json.readTree(detail)).isEqualTo(row);
        }
    }

    @Test
    void categorySearchHandlesNameDescriptionCaseBlankNoMatchAndLiteralWildcards() throws Exception {
        Category child = categories.saveAndFlush(new Category("Technology 100%_ [literal]!",
                "Unique-DESCRIPTION", active.getCategory(), 1));
        String token = login(staff.getAccountEmail(), password).path("accessToken").asText();
        for (String query : new String[]{"  TECHNOLOGY  ", "technology", "unique-description", "UNIQUE-description",
                "%", "_", "[", "!"}) {
            mvc.perform(get("/api/categories").param("q", query).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].categoryId").value(child.getCategoryId()));
        }
        for (String query : new String[]{"", "  "}) {
            mvc.perform(get("/api/categories").param("q", query).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        }
        for (String query : new String[]{"no-such-category", "' OR 1=1 --"}) {
            mvc.perform(get("/api/categories").param("q", query).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        }
        mvc.perform(get("/api/categories").param("q", "x".repeat(201)).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        assertThat(categories.count()).isEqualTo(2);
        assertThat(child.getCategoryDescription()).isEqualTo("Unique-DESCRIPTION");
    }

    @Test
    void categoryDetailRejectsMissingAndInvalidIds() throws Exception {
        String token = login(staff.getAccountEmail(), password).path("accessToken").asText();
        mvc.perform(get("/api/categories/9223372036854775807").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound()).andExpect(jsonPath("code").value("NOT_FOUND"));
        for (String id : new String[]{"0", "-1", "abc", "1.5"}) {
            mvc.perform(get("/api/categories/" + id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void adminAccountListMatchesDatabaseAndOnlyContainsSafeDtoFields() throws Exception {
        String token = login(admin.getAccountEmail(), password).path("accessToken").asText();
        String body = mvc.perform(get("/api/accounts").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsString();
        JsonNode response = json.readTree(body);
        var records = accounts.findAll(org.springframework.data.domain.Sort.by("accountId"));
        assertThat(response.size()).isEqualTo(records.size());
        for (int index = 0; index < records.size(); index++) {
            SystemAccount account = records.get(index);
            JsonNode row = response.get(index);
            assertThat(row.size()).isEqualTo(4);
            assertThat(row.path("accountId").asLong()).isEqualTo(account.getAccountId());
            assertThat(row.path("accountName").asText()).isEqualTo(account.getAccountName());
            assertThat(row.path("accountEmail").asText()).isEqualTo(account.getAccountEmail());
            assertThat(row.path("accountRole").asInt()).isEqualTo(account.getAccountRole());
        }
        assertThat(body).doesNotContain("password", "Password", "$2a$", "$2b$");
    }

    @Test
    void accountSearchMatchesNameEmailCaseAndLiteralWildcards() throws Exception {
        String token = login(admin.getAccountEmail(), password).path("accessToken").asText();
        for (String query : new String[]{" ADMIN TEST ", "ADMIN.TEST@EXAMPLE.COM"}) {
            mvc.perform(get("/api/accounts").param("q", query).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].accountId").value(admin.getAccountId()));
        }
        mvc.perform(get("/api/accounts").param("q", "no-such-account").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        for (String query : new String[]{"", "  "}) {
            mvc.perform(get("/api/accounts").param("q", query).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        }
        accounts.saveAndFlush(new SystemAccount("100%_ [literal]!", "literal@example.com", 2,
                passwords.encode(password)));
        for (String query : new String[]{"%", "_", "[", "!"}) {
            mvc.perform(get("/api/accounts").param("q", query).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        }
        mvc.perform(get("/api/accounts").param("q", "x".repeat(201)).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccountNormalizesEmailHashesPasswordAndReturnsSafeDto() throws Exception {
        String token = login(admin.getAccountEmail(), password).path("accessToken").asText();
        for (int role : new int[]{1, 2}) {
            String email = "created" + role + "@example.com";
            String body = json.writeValueAsString(Map.of("accountName", "  Tài khoản mới  ",
                    "accountEmail", "  " + email.toUpperCase() + "  ", "accountRole", role, "password", "p"));
            String response = mvc.perform(post("/api/accounts").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated()).andExpect(jsonPath("accountName").value("Tài khoản mới"))
                    .andExpect(jsonPath("accountEmail").value(email)).andExpect(jsonPath("accountRole").value(role))
                    .andExpect(header().exists("Location")).andReturn().getResponse().getContentAsString();
            assertThat(json.readTree(response).size()).isEqualTo(4);
            SystemAccount saved = accounts.findByAccountEmailIgnoreCase(email).orElseThrow();
            assertThat(saved.getAccountPassword()).startsWith("$2");
            assertThat(passwords.matches("p", saved.getAccountPassword())).isTrue();
            assertThat(login(email, "p").path("account").path("accountId").asLong()).isEqualTo(saved.getAccountId());
        }
    }

    @Test
    void createAccountRejectsDuplicateEmailAndInvalidFieldsAndTypes() throws Exception {
        String token = login(admin.getAccountEmail(), password).path("accessToken").asText();
        var valid = new java.util.LinkedHashMap<String, Object>(Map.of("accountName", "New account",
                "accountEmail", "new@example.com", "accountRole", 2, "password", password));
        var duplicate = new java.util.LinkedHashMap<>(valid);
        duplicate.put("accountEmail", " STAFF.TEST@EXAMPLE.COM ");
        mvc.perform(post("/api/accounts").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(duplicate)))
                .andExpect(status().isConflict()).andExpect(jsonPath("code").value("CONFLICT"));
        mvc.perform(post("/api/accounts").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("fieldErrors.accountName").exists())
                .andExpect(jsonPath("fieldErrors.accountEmail").exists()).andExpect(jsonPath("fieldErrors.password").exists())
                .andExpect(jsonPath("fieldErrors.accountRole").exists());
        Object[][] invalidValues = {{"accountName", " "}, {"accountName", "x".repeat(101)},
                {"accountEmail", "not-email"}, {"accountEmail", "a".repeat(250) + "@x.com"},
                {"accountRole", 0}, {"accountRole", 3}, {"accountRole", 1.5}, {"accountRole", "1"},
                {"password", " "}, {"password", "é".repeat(37)}, {"password", "x".repeat(73)}};
        for (Object[] invalid : invalidValues) {
            var body = new java.util.LinkedHashMap<>(valid);
            body.put((String) invalid[0], invalid[1]);
            mvc.perform(post("/api/accounts").header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
            mvc.perform(put("/api/accounts/" + staff.getAccountId()).header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }
        assertThat(accounts.count()).isEqualTo(2);
    }

    @Test
    void detailAndUpdateKeepPasswordUnlessANewPasswordIsSupplied() throws Exception {
        String token = login(admin.getAccountEmail(), password).path("accessToken").asText();
        String path = "/api/accounts/" + staff.getAccountId();
        String detail = mvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("accountEmail").value(staff.getAccountEmail()))
                .andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(detail).size()).isEqualTo(4);
        String originalHash = staff.getAccountPassword();
        var body = new java.util.LinkedHashMap<String, Object>(Map.of("accountName", "  Staff đã sửa  ",
                "accountEmail", " UPDATED@EXAMPLE.COM ", "accountRole", 2));
        for (String passwordField : new String[]{"omitted", "empty", "null"}) {
            if (passwordField.equals("empty")) body.put("password", "");
            if (passwordField.equals("null")) body.put("password", null);
            String response = mvc.perform(put(path).header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                    .andExpect(status().isOk()).andExpect(jsonPath("accountName").value("Staff đã sửa"))
                    .andExpect(jsonPath("accountEmail").value("updated@example.com"))
                    .andReturn().getResponse().getContentAsString();
            assertThat(json.readTree(response).size()).isEqualTo(4);
            assertThat(accounts.findById(staff.getAccountId()).orElseThrow().getAccountPassword()).isEqualTo(originalHash);
            login("updated@example.com", password);
        }
        body.put("password", "Changed-password-2026!");
        body.put("accountRole", 1);
        mvc.perform(put(path).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andExpect(jsonPath("accountRole").value(1));
        String changedHash = accounts.findById(staff.getAccountId()).orElseThrow().getAccountPassword();
        assertThat(changedHash).isNotEqualTo(originalHash);
        assertThat(passwords.matches("Changed-password-2026!", changedHash)).isTrue();
        login("updated@example.com", "Changed-password-2026!");
        mvc.perform(loginRequest("updated@example.com", password)).andExpect(status().isUnauthorized());
    }

    @Test
    void updateRejectsDuplicateEmailAndInvalidOrMissingIds() throws Exception {
        String token = login(admin.getAccountEmail(), password).path("accessToken").asText();
        String body = json.writeValueAsString(Map.of("accountName", "Staff", "accountEmail",
                admin.getAccountEmail().toUpperCase(), "accountRole", 2));
        mvc.perform(put("/api/accounts/" + staff.getAccountId()).header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        assertThat(staff.getAccountEmail()).isEqualTo("staff.test@example.com");
        for (String id : new String[]{"0", "-1", "abc", "1.5", "9223372036854775807"}) {
            int expected = 400;
            if (id.equals("9223372036854775807")) expected = 404;
            mvc.perform(get("/api/accounts/" + id).header("Authorization", "Bearer " + token))
                    .andExpect(status().is(expected));
            mvc.perform(put("/api/accounts/" + id).header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().is(expected));
        }
    }

    @Test
    void deleteBlocksCreatorsWithActiveOrOnlyInactiveNews() throws Exception {
        String token = login(admin.getAccountEmail(), password).path("accessToken").asText();
        active.setUpdatedBy(staff);
        articles.saveAndFlush(active);
        for (int status : new int[]{1, 0}) {
            active.setNewsStatus(status);
            articles.saveAndFlush(active);
            mvc.perform(delete("/api/accounts/" + staff.getAccountId()).header("Authorization", "Bearer " + token))
                    .andExpect(status().isConflict()).andExpect(jsonPath("code").value("CONFLICT"));
            assertThat(accounts.existsById(staff.getAccountId())).isTrue();
            assertThat(articles.count()).isEqualTo(2);
            assertThat(jdbc.queryForObject("SELECT UpdatedByID FROM dbo.NewsArticle WHERE NewsArticleID=?",
                    Long.class, active.getNewsArticleId())).isEqualTo(staff.getAccountId());
        }
    }

    @Test
    void deleteClearsUpdaterReferencesAndKeepsNewsAndRelatedData() throws Exception {
        String token = login(admin.getAccountEmail(), password).path("accessToken").asText();
        SystemAccount updater = accounts.saveAndFlush(new SystemAccount("Updater only", "updater@example.com", 2,
                passwords.encode(password)));
        active.setUpdatedBy(updater);
        inactive.setUpdatedBy(updater);
        articles.saveAndFlush(active);
        articles.saveAndFlush(inactive);
        mvc.perform(delete("/api/accounts/" + updater.getAccountId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent()).andExpect(content().string(""));
        assertThat(accounts.existsById(updater.getAccountId())).isFalse();
        for (NewsArticle article : new NewsArticle[]{active, inactive}) {
            assertThat(jdbc.queryForObject("SELECT UpdatedByID FROM dbo.NewsArticle WHERE NewsArticleID=?",
                    Long.class, article.getNewsArticleId())).isNull();
            assertThat(jdbc.queryForObject("SELECT CreatedByID FROM dbo.NewsArticle WHERE NewsArticleID=?",
                    Long.class, article.getNewsArticleId())).isEqualTo(staff.getAccountId());
        }
        assertThat(articles.count()).isEqualTo(2);
        assertThat(categories.count()).isEqualTo(1);
        assertThat(tags.count()).isEqualTo(1);
        assertThat(links.count()).isEqualTo(1);
        for (String id : new String[]{"0", "-1", "abc", "1.5", "9223372036854775807"}) {
            int expected = 400;
            if (id.equals("9223372036854775807")) expected = 404;
            mvc.perform(delete("/api/accounts/" + id).header("Authorization", "Bearer " + token))
                    .andExpect(status().is(expected));
        }
    }

    @Test
    void deleteUnreferencedCurrentAccountIsAllowedAndOldTokenBecomesInvalid() throws Exception {
        String token = login(admin.getAccountEmail(), password).path("accessToken").asText();
        mvc.perform(delete("/api/accounts/" + admin.getAccountId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        assertThat(accounts.existsById(admin.getAccountId())).isFalse();
        assertThat(articles.count()).isEqualTo(2);
        mvc.perform(get("/api/accounts").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test
    void currentDatabaseRoleOverridesOldTokenAndDeletedAccountIsRejected() throws Exception {
        SystemAccount temporary = accounts.saveAndFlush(new SystemAccount("Temporary", "temp@example.com", 1,
                passwords.encode(password)));
        String token = login(temporary.getAccountEmail(), password).path("accessToken").asText();
        temporary.setAccountRole(2);
        accounts.saveAndFlush(temporary);
        mvc.perform(get("/api/accounts").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/news").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
        accounts.delete(temporary);
        accounts.flush();
        mvc.perform(get("/api/news").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }

    @Test
    void publicListAndDetailOnlyExposeActiveNewsEvenInInactiveCategory() throws Exception {
        mvc.perform(get("/api/public/news")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].newsArticleId").value(active.getNewsArticleId()));
        String detail = mvc.perform(get("/api/public/news/" + active.getNewsArticleId()))
                .andExpect(status().isOk()).andExpect(jsonPath("newsContent").value(active.getNewsContent()))
                .andExpect(jsonPath("tags[0].tagName").value("M3"))
                .andReturn().getResponse().getContentAsString();
        assertThat(detail).doesNotContain("accountPassword", "accountEmail", "$2a$");
        mvc.perform(get("/api/public/news/" + inactive.getNewsArticleId())).andExpect(status().isNotFound());
        mvc.perform(get("/api/public/news/9223372036854775807")).andExpect(status().isNotFound());
    }

    @Test
    void publicSearchTrimsCaseFoldsAndSearchesTitleHeadlineContentLiterally() throws Exception {
        for (String query : new String[]{"  TIN m3  ", "unique-head", "UNIQUE-content", "100%_ [public]", "", "  "}) {
            mvc.perform(get("/api/public/news").param("q", query)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
        for (String query : new String[]{"unique-hidden", "no-match", "' OR 1=1 --", "!"}) {
            mvc.perform(get("/api/public/news").param("q", query)).andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
        mvc.perform(get("/api/public/news").param("q", "x".repeat(201))).andExpect(status().isBadRequest());
        for (String id : new String[]{"0", "-1", "abc", "1.5"}) {
            mvc.perform(get("/api/public/news/" + id)).andExpect(status().isBadRequest());
        }
    }

    @Test
    void demoSeedIsAdditiveIdempotentAndStoresBcrypt() {
        long oldAccounts = accounts.count();
        long oldArticles = articles.count();
        DemoDataInitializer seed = new DemoDataInitializer(accounts, categories, articles, passwords, validator,
                "demo.admin@example.com", password, "demo.staff@example.com", password);
        seed.run(new DefaultApplicationArguments());
        accounts.flush();
        articles.flush();
        seed.run(new DefaultApplicationArguments());
        assertThat(accounts.count()).isEqualTo(oldAccounts + 2);
        assertThat(articles.count()).isEqualTo(oldArticles + 2);
        assertThat(articles.findById(active.getNewsArticleId()).orElseThrow().getNewsTitle()).isEqualTo(active.getNewsTitle());
        SystemAccount demo = accounts.findByAccountEmailIgnoreCase("demo.admin@example.com").orElseThrow();
        assertThat(passwords.matches(password, demo.getAccountPassword())).isTrue();
        String hash = demo.getAccountPassword();
        demo.setAccountName("User edited name");
        seed.run(new DefaultApplicationArguments());
        assertThat(demo.getAccountName()).isEqualTo("User edited name");
        assertThat(demo.getAccountPassword()).isEqualTo(hash);
    }

    @Test
    void corsOnlyAllowsConfiguredFrontendAndUnknownRoutesAreDenied() throws Exception {
        mvc.perform(options("/api/public/news").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mvc.perform(options("/api/public/news").header("Origin", "https://untrusted.example")
                .header("Access-Control-Request-Method", "GET")).andExpect(status().isForbidden());
        mvc.perform(get("/api/unknown")).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidJwtKeyConfigurationIsRejected() {
        var configuration = new fpt.edu.a2phantuanthanh_se1910.security.JwtConfiguration();
        assertThatThrownBy(() -> configuration.jwtSecretKey("not-base64!"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> configuration.jwtSecretKey("c2hvcnQ="))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void profileReturnsOnlyAuthenticatedStaffEvenWhenAnotherAccountIdIsSupplied() throws Exception {
        SystemAccount otherStaff = accounts.saveAndFlush(new SystemAccount("Staff thứ hai",
                "second.profile@example.com", 2, passwords.encode(password)));
        for (SystemAccount account : new SystemAccount[]{staff, otherStaff}) {
            String accessToken = login(account.getAccountEmail(), password).path("accessToken").asText();
            long otherId = staff.getAccountId();
            if (account.getAccountId().equals(staff.getAccountId())) {
                otherId = otherStaff.getAccountId();
            }
            String body = mvc.perform(get("/api/profile")
                            .param("accountId", Long.toString(otherId)).param("id", Long.toString(otherId))
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                    .andReturn().getResponse().getContentAsString();
            JsonNode response = json.readTree(body);
            SystemAccount stored = accounts.findById(account.getAccountId()).orElseThrow();
            assertThat(response.size()).isEqualTo(4);
            assertThat(response.path("accountId").asLong()).isEqualTo(stored.getAccountId());
            assertThat(response.path("accountName").asText()).isEqualTo(stored.getAccountName());
            assertThat(response.path("accountEmail").asText()).isEqualTo(stored.getAccountEmail());
            assertThat(response.path("accountRole").asInt()).isEqualTo(2);
            assertThat(body).doesNotContain("password", "Password", stored.getAccountPassword());
        }
    }

    @Test
    void profileUsesCurrentDatabaseValuesAndRevokesAccessAfterRoleChangeOrDeletion() throws Exception {
        SystemAccount account = accounts.saveAndFlush(new SystemAccount("Profile before change",
                "current.profile@example.com", 2, passwords.encode(password)));
        String accessToken = login(account.getAccountEmail(), password).path("accessToken").asText();
        account.setAccountName("Tên hồ sơ mới");
        account.setAccountEmail("changed.profile@example.com");
        accounts.saveAndFlush(account);
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("accountName").value("Tên hồ sơ mới"))
                .andExpect(jsonPath("accountEmail").value("changed.profile@example.com"));
        account.setAccountRole(1);
        accounts.saveAndFlush(account);
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
        accounts.delete(account);
        accounts.flush();
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void profileRejectsGuestsInvalidExpiredTokensAndAdmin() throws Exception {
        mvc.perform(get("/api/profile")).andExpect(status().isUnauthorized());
        for (String accessToken : new String[]{"bad-token",
                token(staff, Instant.now().minusSeconds(2), "A2PhanTuanThanh_SE1910")}) {
            mvc.perform(get("/api/profile").header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isUnauthorized());
        }
        String adminToken = login(admin.getAccountEmail(), password).path("accessToken").asText();
        mvc.perform(get("/api/profile").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    private JsonNode login(String email, String password) throws Exception {
        String body = mvc.perform(loginRequest(" " + email + " ", password)).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private MockHttpServletRequestBuilder loginRequest(String email, String password) throws Exception {
        return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", password)));
    }

    private String token(SystemAccount account, Instant expiresAt, String issuer) {
        JwtClaimsSet claims = JwtClaimsSet.builder().subject(account.getAccountId().toString()).issuer(issuer)
                .issuedAt(Instant.now().minusSeconds(120)).expiresAt(expiresAt).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
