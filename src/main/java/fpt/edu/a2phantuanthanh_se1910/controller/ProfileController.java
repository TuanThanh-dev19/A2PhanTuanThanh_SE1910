// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.controller;

import fpt.edu.a2phantuanthanh_se1910.dto.response.AccountResponse;
import fpt.edu.a2phantuanthanh_se1910.service.AuthService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final AuthService authService;

    public ProfileController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<AccountResponse> findCurrentProfile(Principal principal) {
        // Identity comes from the verified JWT, never from a client-supplied account ID.
        long accountId = Long.parseLong(principal.getName());
        AccountResponse response = authService.findCurrentAccount(accountId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response);
    }
}
