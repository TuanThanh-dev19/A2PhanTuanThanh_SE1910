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
    void roleRulesProtectReadsAndWritesWithoutFakeCrudControllers() throws Exception {
        String adminToken = login(admin.getAccountEmail(), password).path("accessToken").asText();
        String staffToken = login(staff.getAccountEmail(), password).path("accessToken").asText();
        String[] staffRoutes = {"/api/categories", "/api/categories/1", "/api/news", "/api/news/1",
                "/api/tags", "/api/profile", "/api/profile/news"};
        for (String route : staffRoutes) {
            for (String method : new String[]{"GET", "POST", "PUT", "DELETE"}) {
                mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), route)
                                .header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isForbidden()).andExpect(jsonPath("code").value("FORBIDDEN"));
                mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), route))
                        .andExpect(status().isUnauthorized());
            }
            // No controller exists yet: reaching MVC's 404 proves the authorized request passed the filter.
            mvc.perform(get(route).header("Authorization", "Bearer " + staffToken)).andExpect(status().isNotFound());
        }
        for (String route : new String[]{"/api/accounts", "/api/accounts/1"}) {
            for (String method : new String[]{"GET", "POST", "PUT", "DELETE"}) {
                mvc.perform(request(org.springframework.http.HttpMethod.valueOf(method), route)
                                .header("Authorization", "Bearer " + staffToken)).andExpect(status().isForbidden());
            }
            mvc.perform(get(route).header("Authorization", "Bearer " + adminToken)).andExpect(status().isNotFound());
        }
    }

    @Test
    void currentDatabaseRoleOverridesOldTokenAndDeletedAccountIsRejected() throws Exception {
        SystemAccount temporary = accounts.saveAndFlush(new SystemAccount("Temporary", "temp@example.com", 1,
                passwords.encode(password)));
        String token = login(temporary.getAccountEmail(), password).path("accessToken").asText();
        temporary.setAccountRole(2);
        accounts.saveAndFlush(temporary);
        mvc.perform(get("/api/accounts").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/news").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
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
