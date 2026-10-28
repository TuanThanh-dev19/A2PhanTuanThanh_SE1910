// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.response.AccountResponse;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AccountService {
    private final SystemAccountRepository accounts;

    public AccountService(SystemAccountRepository accounts) {
        this.accounts = accounts;
    }

    public List<AccountResponse> findAll() {
        List<SystemAccount> matches = accounts.findAll(Sort.by("accountId"));
        List<AccountResponse> response = new ArrayList<>();
        for (SystemAccount account : matches) {
            response.add(new AccountResponse(account.getAccountId(), account.getAccountName(),
                    account.getAccountEmail(), account.getAccountRole()));
        }
        return response;
    }
}
