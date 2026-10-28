// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.controller;

import fpt.edu.a2phantuanthanh_se1910.dto.response.AccountResponse;
import fpt.edu.a2phantuanthanh_se1910.dto.request.CreateAccountRequest;
import fpt.edu.a2phantuanthanh_se1910.dto.request.UpdateAccountRequest;
import fpt.edu.a2phantuanthanh_se1910.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.net.URI;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> search(@RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(accountService.search(q));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.create(request);
        return ResponseEntity.created(URI.create("/api/accounts/" + response.accountId()))
                .cacheControl(CacheControl.noStore()).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable @Positive long id) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(accountService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(@PathVariable @Positive long id,
                                                 @Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(accountService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
