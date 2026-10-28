// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.bootstrap;

import fpt.edu.a2phantuanthanh_se1910.entity.Category;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsArticle;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.repository.CategoryRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsArticleRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {
    private final SystemAccountRepository accounts;
    private final CategoryRepository categories;
    private final NewsArticleRepository articles;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final String adminEmail;
    private final String adminPassword;
    private final String staffEmail;
    private final String staffPassword;

    public DemoDataInitializer(SystemAccountRepository accounts, CategoryRepository categories,
                               NewsArticleRepository articles, PasswordEncoder passwordEncoder, Validator validator,
                               @Value("${app.demo.admin-email}") String adminEmail,
                               @Value("${app.demo.admin-password}") String adminPassword,
                               @Value("${app.demo.staff-email}") String staffEmail,
                               @Value("${app.demo.staff-password}") String staffPassword) {
        this.accounts = accounts;
        this.categories = categories;
        this.articles = articles;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
        this.adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        this.adminPassword = adminPassword;
        this.staffEmail = staffEmail.trim().toLowerCase(Locale.ROOT);
        this.staffPassword = staffPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (adminEmail.equals(staffEmail)) {
            throw new IllegalStateException("Demo Admin and Staff emails must differ");
        }
        createMissingAccount("M3 Demo Admin", adminEmail, 1, adminPassword);
        SystemAccount staff = createMissingAccount("M3 Demo Staff", staffEmail, 2, staffPassword);
        Category category = categories.findFirstByCategoryName("M3 Demo")
                .orElseGet(() -> categories.save(new Category("M3 Demo", "Dữ liệu demo M3", null, 1)));
        createMissingArticle("M3 Demo - Tin công khai", "Bài active cho khách đọc", 1, category, staff);
        createMissingArticle("M3 Demo - Tin nội bộ", "Bài inactive không công khai", 0, category, staff);
    }

    private SystemAccount createMissingAccount(String name, String email, int role, String password) {
        var existing = accounts.findByAccountEmailIgnoreCase(email);
        if (existing.isPresent()) {
            if (existing.get().getAccountRole() != role) {
                throw new IllegalStateException("Demo email already has a different role; choose another demo email");
            }
            return existing.get();
        }
        if (password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72 || password.isBlank()) {
            throw new IllegalStateException("New demo passwords require at least 8 characters and at most 72 UTF-8 bytes");
        }
        SystemAccount account = new SystemAccount(name, email, role, passwordEncoder.encode(password));
        if (!validator.validate(account).isEmpty()) {
            throw new IllegalStateException("Invalid demo account configuration");
        }
        return accounts.save(account);
    }

    private void createMissingArticle(String title, String headline, int status, Category category, SystemAccount staff) {
        if (!articles.existsByNewsTitleAndCreatedBy_AccountId(title, staff.getAccountId())) {
            articles.save(new NewsArticle(title, headline, LocalDateTime.now(ZoneOffset.UTC),
                    "Nội dung tiếng Việt để kiểm tra đăng nhập và tin công khai M3.", "M3 demo",
                    category, status, staff));
        }
    }
}
