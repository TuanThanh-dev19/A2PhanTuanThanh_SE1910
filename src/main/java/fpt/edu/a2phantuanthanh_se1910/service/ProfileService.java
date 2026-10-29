// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.request.UpdateProfileRequest;
import fpt.edu.a2phantuanthanh_se1910.dto.response.AccountResponse;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

@Service
@Transactional(readOnly = true)
public class ProfileService {
    private final SystemAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(SystemAccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountResponse findCurrentProfile(long accountId) {
        return mapAccount(requireAccount(accountId));
    }

    @Transactional
    public AccountResponse update(long accountId, UpdateProfileRequest request) {
        SystemAccount account = requireAccount(accountId);
        if (accounts.existsByAccountEmailIgnoreCaseAndAccountIdNot(request.accountEmail(), accountId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }
        // Empty or missing password keeps the current hash. Identity and role are never writable here.
        String password = request.password();
        if (password != null && !password.isEmpty()) {
            if (password.isBlank() || password.length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "New password must contain at least 8 characters and must not be blank");
            }
            if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Password must be at most 72 UTF-8 bytes");
            }
            account.setAccountPassword(passwordEncoder.encode(password));
        }
        account.setAccountName(request.accountName());
        account.setAccountEmail(request.accountEmail());
        accounts.saveAndFlush(account);
        return mapAccount(account);
    }

    private SystemAccount requireAccount(long accountId) {
        return accounts.findById(accountId)
                .orElseThrow(() -> new InvalidBearerTokenException("Account no longer exists"));
    }

    private AccountResponse mapAccount(SystemAccount account) {
        return new AccountResponse(account.getAccountId(), account.getAccountName(),
                account.getAccountEmail(), account.getAccountRole());
    }
}
