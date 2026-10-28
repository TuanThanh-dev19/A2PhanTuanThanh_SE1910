// Author: Phan Tuan Thanh - CE190595

package fpt.edu.a2phantuanthanh_se1910.persistence;

import fpt.edu.a2phantuanthanh_se1910.A2PhanTuanThanhSe1910Application;
import fpt.edu.a2phantuanthanh_se1910.entity.Category;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsArticle;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsTag;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsTagId;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.entity.Tag;
import fpt.edu.a2phantuanthanh_se1910.repository.CategoryRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsArticleRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsTagRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.TagRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NewsPersistenceTests {

    private ConfigurableApplicationContext context;
    private JdbcTemplate jdbc;
    private TransactionTemplate transaction;
    private SystemAccountRepository accounts;
    private CategoryRepository categories;
    private NewsArticleRepository articles;
    private TagRepository tags;
    private NewsTagRepository links;
    private PersistenceFixture.FixtureIds ids;
    private String databaseName;

    @BeforeAll
    void startOnExplicitIsolatedDatabase() throws Exception {
        databaseName = System.getenv("SQLSERVER_TEST_DATABASE");
        if (databaseName == null || !databaseName.matches("A2PhanTuanThanh_SE1910_M2Test_[A-Za-z0-9_]+")) {
            throw new IllegalStateException("Run scripts/test-persistence.ps1 to select an isolated M2 database");
        }
        startApplication("update");
        assertThat(jdbc.queryForObject("SELECT DB_NAME()", String.class)).isEqualTo(databaseName);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM dbo.NewsArticle", Long.class)).isZero();
    }

    private void startApplication(String schemaMode) throws Exception {
        // Reuse the verified connection settings, changing only databaseName/applicationName.
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(new ResourcePropertySource("classpath:application.properties"));
        String originalUrl = environment.getRequiredProperty("spring.datasource.url");
        if (!originalUrl.contains("databaseName=A2PhanTuanThanh_SE1910;")) {
            throw new IllegalStateException("Assignment datasource URL is required for isolated tests");
        }
        String testUrl = originalUrl.replace("databaseName=A2PhanTuanThanh_SE1910;",
                "databaseName=" + databaseName + ";");
        context = new SpringApplicationBuilder(A2PhanTuanThanhSe1910Application.class)
                .web(WebApplicationType.NONE)
                .run("--spring.datasource.url=" + testUrl,
                        "--spring.jpa.hibernate.ddl-auto=" + schemaMode,
                        "--spring.main.banner-mode=off");
        jdbc = context.getBean(JdbcTemplate.class);
        transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        accounts = context.getBean(SystemAccountRepository.class);
        categories = context.getBean(CategoryRepository.class);
        articles = context.getBean(NewsArticleRepository.class);
        tags = context.getBean(TagRepository.class);
        links = context.getBean(NewsTagRepository.class);
    }

    @AfterAll
    void stopApplication() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    @Order(1)
    void jpaCreatesExactTablesColumnsKeysAndConstraints() {
        assertDatabaseSchema();
        System.out.println("TC-DB-01 PASS: 5 tables, 26 columns, 5 PKs, 6 FKs, 2 unique constraints, 3 checks");
    }

    private void assertDatabaseSchema() {
        assertThat(jdbc.queryForList("SELECT name FROM sys.tables WHERE is_ms_shipped = 0", String.class))
                .containsExactlyInAnyOrder("SystemAccount", "Category", "NewsArticle", "Tag", "NewsTag");
        List<String> columns = jdbc.queryForList("""
                SELECT t.name + '.' + c.name + ':' + TYPE_NAME(c.user_type_id) + ':'
                    + CAST(c.max_length AS varchar(10)) + ':' + CAST(c.is_nullable AS varchar(1))
                FROM sys.tables t JOIN sys.columns c ON c.object_id = t.object_id
                WHERE t.is_ms_shipped = 0
                """, String.class);
        // SQL Server reports nvarchar length in bytes; -1 means nvarchar(max).
        assertThat(columns).containsExactlyInAnyOrder(
                "SystemAccount.AccountID:bigint:8:0", "SystemAccount.AccountName:nvarchar:200:0",
                "SystemAccount.AccountEmail:nvarchar:508:0", "SystemAccount.AccountRole:int:4:0",
                "SystemAccount.AccountPassword:nvarchar:510:0",
                "Category.CategoryID:bigint:8:0", "Category.CategoryName:nvarchar:200:0",
                "Category.CategoryDescription:nvarchar:2000:1", "Category.ParentCategoryID:bigint:8:1",
                "Category.IsActive:int:4:0",
                "NewsArticle.NewsArticleID:bigint:8:0", "NewsArticle.NewsTitle:nvarchar:400:0",
                "NewsArticle.Headline:nvarchar:600:0", "NewsArticle.CreatedDate:datetime2:8:0",
                "NewsArticle.NewsContent:nvarchar:-1:0", "NewsArticle.NewsSource:nvarchar:1000:1",
                "NewsArticle.CategoryID:bigint:8:0", "NewsArticle.NewsStatus:int:4:0",
                "NewsArticle.CreatedByID:bigint:8:0", "NewsArticle.UpdatedByID:bigint:8:1",
                "NewsArticle.ModifiedDate:datetime2:8:1", "Tag.TagID:bigint:8:0",
                "Tag.TagName:nvarchar:200:0", "Tag.Note:nvarchar:1000:1",
                "NewsTag.NewsArticleID:bigint:8:0", "NewsTag.TagID:bigint:8:0");

        assertThat(jdbc.queryForList("""
                SELECT t.name + '.' + c.name FROM sys.tables t
                JOIN sys.indexes i ON i.object_id=t.object_id AND i.is_primary_key=1
                JOIN sys.index_columns ic ON ic.object_id=i.object_id AND ic.index_id=i.index_id
                JOIN sys.columns c ON c.object_id=ic.object_id AND c.column_id=ic.column_id
                """, String.class)).containsExactlyInAnyOrder("SystemAccount.AccountID", "Category.CategoryID",
                "NewsArticle.NewsArticleID", "Tag.TagID", "NewsTag.NewsArticleID", "NewsTag.TagID");
        assertThat(jdbc.queryForList("""
                SELECT OBJECT_NAME(f.parent_object_id) + '.' + COL_NAME(f.parent_object_id,f.parent_column_id)
                    + '->' + OBJECT_NAME(f.referenced_object_id) + '.'
                    + COL_NAME(f.referenced_object_id,f.referenced_column_id)
                FROM sys.foreign_key_columns f
                """, String.class)).containsExactlyInAnyOrder("Category.ParentCategoryID->Category.CategoryID",
                "NewsArticle.CategoryID->Category.CategoryID", "NewsArticle.CreatedByID->SystemAccount.AccountID",
                "NewsArticle.UpdatedByID->SystemAccount.AccountID", "NewsTag.NewsArticleID->NewsArticle.NewsArticleID",
                "NewsTag.TagID->Tag.TagID");
        assertThat(jdbc.queryForList("SELECT name FROM sys.key_constraints WHERE type='UQ'", String.class))
                .containsExactlyInAnyOrder("UQ_NewsTag_TagID", "UQ_SystemAccount_AccountEmail");
        assertThat(jdbc.queryForList("SELECT name FROM sys.check_constraints", String.class))
                .containsExactlyInAnyOrder("CK_Category_StatusAndParent", "CK_SystemAccount_Role", "CK_NewsArticle_Status");
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM sys.identity_columns c
                JOIN sys.tables t ON t.object_id=c.object_id WHERE t.is_ms_shipped=0
                """, Integer.class)).isEqualTo(4);
        assertThat(jdbc.queryForObject("""
                SELECT COUNT(*) FROM sys.foreign_keys
                WHERE delete_referential_action <> 0 OR is_disabled=1 OR is_not_trusted=1
                """, Integer.class)).isZero();
    }

    @Test
    @Order(2)
    void repositoriesRoundTripVietnameseRelationshipsAndQueries() {
        ids = transaction.execute(status -> new PersistenceFixture(accounts, categories, articles, tags, links).create());
        assertFixtureAndQueries();
    }

    private void assertFixtureAndQueries() {
        assertDatabaseSchema();
        transaction.executeWithoutResult(status -> {
            NewsArticle active = articles.findById(ids.active()).orElseThrow();
            assertThat(active.getNewsTitle()).isEqualTo(PersistenceFixture.TITLE);
            assertThat(active.getHeadline()).isEqualTo("Khám phá ý tưởng mới");
            assertThat(active.getNewsContent()).isEqualTo(PersistenceFixture.CONTENT);
            assertThat(active.getNewsSource()).isEqualTo("Bản tin Đại học FPT");
            assertThat(active.getCreatedDate()).isEqualTo(PersistenceFixture.CREATED_AT);
            assertThat(active.getModifiedDate()).isEqualTo(PersistenceFixture.CREATED_AT.plusHours(1));
            assertThat(active.getCategory().getCategoryName()).isEqualTo("Đời sống sinh viên");
            assertThat(active.getCategory().getParentCategory().getCategoryName()).isEqualTo("Giáo dục");
            assertThat(active.getCategory().getIsActive()).isZero();
            assertThat(active.getCreatedBy().getAccountName()).isEqualTo("Nguyễn Thị Ánh");
            assertThat(active.getUpdatedBy().getAccountName()).isEqualTo("Trần Tuấn Đạt");
            NewsArticle inactive = articles.findById(ids.inactive()).orElseThrow();
            assertThat(inactive.getUpdatedBy()).isNull();
            assertThat(inactive.getModifiedDate()).isNull();
            assertThat(inactive.getNewsSource()).isNull();
            assertThat(categories.findById(ids.parent()).orElseThrow().getParentCategory()).isNull();
            assertThat(categories.findById(ids.unusedCategory()).orElseThrow().getCategoryDescription()).isNull();
            assertThat(tags.findById(ids.linkedTag()).orElseThrow().getTagName()).isEqualTo("Sáng tạo");
            assertThat(tags.findById(ids.linkedTag()).orElseThrow().getNote()).isEqualTo("Nhãn tiếng Việt");
            assertThat(tags.findById(ids.freeTag()).orElseThrow().getNote()).isNull();
            assertThat(accounts.findByAccountEmailIgnoreCase("ANH@EXAMPLE.INVALID").orElseThrow().getAccountId())
                    .isEqualTo(ids.creator());
            assertThat(accounts.existsByAccountEmailIgnoreCase("ANH@EXAMPLE.INVALID")).isTrue();
            assertThat(accounts.existsByAccountEmailIgnoreCase("absent@example.invalid")).isFalse();
            assertThat(categories.findByParentCategory_CategoryId(ids.parent())).extracting(Category::getCategoryId)
                    .containsExactly(ids.child());
            assertThat(articles.existsByCategory_CategoryId(ids.child())).isTrue();
            assertThat(articles.existsByCategory_CategoryId(ids.unusedCategory())).isFalse();
            assertThat(articles.existsByCreatedBy_AccountId(ids.creator())).isTrue();
            assertThat(articles.existsByCreatedBy_AccountId(ids.admin())).isFalse();
            assertThat(articles.existsByUpdatedBy_AccountId(ids.updater())).isTrue();
            assertThat(articles.existsByUpdatedBy_AccountId(ids.creator())).isFalse();
            assertThat(articles.findByCreatedBy_AccountIdOrderByCreatedDateDescNewsArticleIdDesc(ids.creator()))
                    .extracting(NewsArticle::getNewsArticleId).containsExactly(ids.inactive(), ids.active());
            assertThat(articles.findByCreatedBy_AccountIdOrderByCreatedDateDescNewsArticleIdDesc(ids.updater()))
                    .extracting(NewsArticle::getNewsArticleId).containsExactly(ids.otherCreator());
            assertThat(articles.findByUpdatedBy_AccountId(ids.updater()))
                    .extracting(NewsArticle::getNewsArticleId).containsExactly(ids.active());
            assertThat(articles.findByNewsStatusOrderByCreatedDateDescNewsArticleIdDesc(1))
                    .extracting(NewsArticle::getNewsArticleId).containsExactly(ids.otherCreator(), ids.active());
            assertThat(articles.findByNewsArticleIdAndNewsStatus(ids.inactive(), 1)).isEmpty();
            assertThat(articles.findByNewsArticleIdAndNewsStatus(ids.active(), 1)).isPresent();
            assertThat(links.findByNewsArticle_NewsArticleId(ids.active())).hasSize(2);
            assertThat(links.findByNewsArticle_NewsArticleId(ids.inactive())).isEmpty();
            NewsTag linked = links.findById(new NewsTagId(ids.active(), ids.linkedTag())).orElseThrow();
            assertThat(linked.getNewsArticle().getNewsArticleId()).isEqualTo(ids.active());
            assertThat(linked.getTag().getTagId()).isEqualTo(ids.linkedTag());
            assertThat(links.existsByTag_TagId(ids.freeTag())).isFalse();
            assertThat(links.existsByTag_TagId(ids.linkedTag())).isTrue();
            assertThat(links.findByTag_TagId(ids.linkedTag()).orElseThrow().getId()).isEqualTo(linked.getId());
            assertThat(accounts.count()).isEqualTo(3);
            assertThat(categories.count()).isEqualTo(3);
            assertThat(articles.count()).isEqualTo(3);
            assertThat(tags.count()).isEqualTo(3);
            assertThat(links.count()).isEqualTo(2);
        });
    }

    @Test
    @Order(3)
    void committedDataSurvivesApplicationRestartAndSchemaValidation() throws Exception {
        context.close();
        startApplication("validate");
        assertFixtureAndQueries();
        context.close();
        startApplication("update");
        assertFixtureAndQueries();
        System.out.println("TC-DB-02 PASS: committed Vietnamese fixture survives two application context restarts (validate/update)");
    }

    @Test
    @Order(4)
    void databaseRejectsEveryInvalidForeignKeyAndReferencedDeletes() {
        assertSqlRejected(547, "UPDATE dbo.Category SET ParentCategoryID=-1 WHERE CategoryID=?", ids.child());
        assertSqlRejected(547, "UPDATE dbo.NewsArticle SET CategoryID=-1 WHERE NewsArticleID=?", ids.active());
        assertSqlRejected(547, "UPDATE dbo.NewsArticle SET CreatedByID=-1 WHERE NewsArticleID=?", ids.active());
        assertSqlRejected(547, "UPDATE dbo.NewsArticle SET UpdatedByID=-1 WHERE NewsArticleID=?", ids.active());
        assertSqlRejected(547, "INSERT INTO dbo.NewsTag (NewsArticleID,TagID) VALUES (-1,?)", ids.freeTag());
        assertSqlRejected(547, "INSERT INTO dbo.NewsTag (NewsArticleID,TagID) VALUES (?,-1)", ids.inactive());
        assertSqlRejected(547, "DELETE FROM dbo.SystemAccount WHERE AccountID=?", ids.creator());
        assertSqlRejected(547, "DELETE FROM dbo.Category WHERE CategoryID=?", ids.child());
        assertFixtureAndQueries();
    }

    @Test
    @Order(5)
    void databaseRejectsDuplicateLinksTagReuseEmailAndInvalidDomains() {
        assertSqlRejected(2627, "INSERT INTO dbo.NewsTag (NewsArticleID,TagID) VALUES (?,?)", ids.active(), ids.linkedTag());
        assertSqlRejected(2627, "INSERT INTO dbo.NewsTag (NewsArticleID,TagID) VALUES (?,?)", ids.inactive(), ids.linkedTag());
        assertSqlRejected(2627, "UPDATE dbo.SystemAccount SET AccountEmail='anh@example.invalid' WHERE AccountID=?", ids.admin());
        assertSqlRejected(547, "UPDATE dbo.SystemAccount SET AccountRole=3 WHERE AccountID=?", ids.admin());
        assertSqlRejected(547, "UPDATE dbo.NewsArticle SET NewsStatus=2 WHERE NewsArticleID=?", ids.active());
        assertSqlRejected(547, "UPDATE dbo.Category SET IsActive=2 WHERE CategoryID=?", ids.child());
        assertSqlRejected(547, "UPDATE dbo.Category SET ParentCategoryID=CategoryID WHERE CategoryID=?", ids.child());
        assertSqlRejected(515, "UPDATE dbo.NewsArticle SET CategoryID=NULL WHERE NewsArticleID=?", ids.active());
        assertSqlRejected(515, "UPDATE dbo.NewsArticle SET CreatedByID=NULL WHERE NewsArticleID=?", ids.active());
        assertSqlRejected(515, "UPDATE dbo.NewsArticle SET NewsContent=NULL WHERE NewsArticleID=?", ids.active());
        assertFixtureAndQueries();
    }

    private void assertSqlRejected(int expectedError, String sql, Object... parameters) {
        Throwable failure = catchThrowable(() -> transaction.executeWithoutResult(status -> jdbc.update(sql, parameters)));
        assertThat(failure).isInstanceOf(DataIntegrityViolationException.class);
        Throwable root = failure;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        assertThat(root).isInstanceOf(SQLException.class);
        assertThat(((SQLException) root).getErrorCode()).as(sql).isEqualTo(expectedError);
    }

    @Test
    @Order(6)
    void repositoryValidationRejectsBlankOversizeEmailRoleStatusAndNullRelation() {
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> accounts.saveAndFlush(
                new SystemAccount(" ", "bad-email", 3, UUID.randomUUID().toString()))))
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> tags.saveAndFlush(
                new Tag("a".repeat(101), null)))).isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            NewsArticle article = articles.findById(ids.active()).orElseThrow();
            article.setNewsStatus(2);
            article.setCategory(null);
            articles.flush();
        })).isInstanceOf(ConstraintViolationException.class);
        assertFixtureAndQueries();
    }

    @Test
    @Order(7)
    void failedLinkRollsBackFlushedArticleAndNewTag() {
        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            NewsArticle article = articles.findById(ids.active()).orElseThrow();
            article.setNewsTitle("Thay đổi phải rollback");
            articles.flush();
            tags.saveAndFlush(new Tag("Tag cũng phải rollback", "Chưa commit"));
            jdbc.update("INSERT INTO dbo.NewsTag (NewsArticleID,TagID) VALUES (?,?)", ids.inactive(), ids.linkedTag());
        })).isInstanceOf(DataIntegrityViolationException.class);
        assertFixtureAndQueries();

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            NewsArticle article = articles.findById(ids.active()).orElseThrow();
            article.setNewsTitle("Rollback khi sai FK");
            articles.flush();
            Tag newTag = tags.saveAndFlush(new Tag("Tag tạm trước lỗi FK", null));
            NewsArticle missingArticle = articles.getReferenceById(Long.MAX_VALUE);
            links.saveAndFlush(new NewsTag(missingArticle, newTag));
        })).isInstanceOf(DataIntegrityViolationException.class);
        assertFixtureAndQueries();
        System.out.println("TC-DB-03 PASS: all FKs, duplicate link/tag limit, domains and validation rejected; flushed writes rolled back");
    }
}
