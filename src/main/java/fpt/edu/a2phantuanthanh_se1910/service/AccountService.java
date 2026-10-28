// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.response.AccountResponse;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AccountService {
    private final SystemAccountRepository accounts;

    public AccountService(SystemAccountRepository accounts) {
        this.accounts = accounts;
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
            response.add(new AccountResponse(account.getAccountId(), account.getAccountName(),
                    account.getAccountEmail(), account.getAccountRole()));
        }
        return response;
    }
}
