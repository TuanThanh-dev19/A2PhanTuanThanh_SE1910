// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.response.AccountResponse;
import fpt.edu.a2phantuanthanh_se1910.dto.request.CreateAccountRequest;
import fpt.edu.a2phantuanthanh_se1910.dto.request.UpdateAccountRequest;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.entity.NewsArticle;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
import fpt.edu.a2phantuanthanh_se1910.repository.NewsArticleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
@Transactional(readOnly = true)
public class AccountService {
    private final SystemAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final NewsArticleRepository newsArticles;

    public AccountService(SystemAccountRepository accounts, PasswordEncoder passwordEncoder,
                          NewsArticleRepository newsArticles) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.newsArticles = newsArticles;
    }

    public List<AccountResponse> search(String query) {
        String keyword = query.trim();
        if (keyword.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search must be at most 200 characters");
        }
        // Search user text literally instead of treating LIKE wildcards as patterns.
        keyword = keyword.toLowerCase(Locale.ROOT).replace("!", "!!")
                .replace("%", "!%").replace("_", "!_").replace("[", "![");
        List<SystemAccount> matches = accounts.search(keyword);
        List<AccountResponse> response = new ArrayList<>();
        for (SystemAccount account : matches) {
            response.add(mapAccount(account));
        }
        return response;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        if (accounts.existsByAccountEmailIgnoreCase(request.accountEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }
        validatePassword(request.password());
        String hash = passwordEncoder.encode(request.password());
        SystemAccount account = new SystemAccount(request.accountName(), request.accountEmail(),
                request.accountRole(), hash);
        accounts.saveAndFlush(account);
        return mapAccount(account);
    }

    public AccountResponse findById(long id) {
        return mapAccount(requireAccount(id));
    }

    @Transactional
    public AccountResponse update(long id, UpdateAccountRequest request) {
        SystemAccount account = requireAccount(id);
        if (accounts.existsByAccountEmailIgnoreCaseAndAccountIdNot(request.accountEmail(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }
        // Missing or empty password preserves the existing BCrypt hash exactly.
        if (request.password() != null && !request.password().isEmpty()) {
            validatePassword(request.password());
            account.setAccountPassword(passwordEncoder.encode(request.password()));
        }
        account.setAccountName(request.accountName());
        account.setAccountEmail(request.accountEmail());
        account.setAccountRole(request.accountRole());
        accounts.saveAndFlush(account);
        return mapAccount(account);
    }

    @Transactional
    public void delete(long id) {
        SystemAccount account = requireAccount(id);
        if (newsArticles.existsByCreatedBy_AccountId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete an account that has created articles, including inactive articles.");
        }
        List<NewsArticle> editedArticles = newsArticles.findByUpdatedBy_AccountId(id);
        for (NewsArticle article : editedArticles) {
            article.setUpdatedBy(null);
        }
        // DEC-06: release updater references before deleting; both steps roll back on failure.
        newsArticles.flush();
        accounts.delete(account);
        accounts.flush();
    }

    private void validatePassword(String password) {
        if (password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must not be blank");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }
    }

    private SystemAccount requireAccount(long id) {
        return accounts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private AccountResponse mapAccount(SystemAccount account) {
        return new AccountResponse(account.getAccountId(), account.getAccountName(),
                account.getAccountEmail(), account.getAccountRole());
    }
}
