// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.controller;

import fpt.edu.a2phantuanthanh_se1910.dto.response.AccountResponse;
import fpt.edu.a2phantuanthanh_se1910.dto.request.UpdateProfileRequest;
import fpt.edu.a2phantuanthanh_se1910.service.ProfileService;
import fpt.edu.a2phantuanthanh_se1910.dto.response.NewsSummary;
import fpt.edu.a2phantuanthanh_se1910.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final NewsService newsService;

    public ProfileController(ProfileService profileService, NewsService newsService) {
        this.profileService = profileService;
        this.newsService = newsService;
    }

    @GetMapping
    public ResponseEntity<AccountResponse> findCurrentProfile(Principal principal) {
        // Identity comes from the verified JWT, never from a client-supplied account ID.
        long accountId = Long.parseLong(principal.getName());
        AccountResponse response = profileService.findCurrentProfile(accountId);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response);
    }

    @PutMapping
    public ResponseEntity<AccountResponse> update(@Valid @RequestBody UpdateProfileRequest request,
                                                  Principal principal) {
        long accountId = Long.parseLong(principal.getName());
        AccountResponse response = profileService.update(accountId, request);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(response);
    }

    @GetMapping("/news")
    public ResponseEntity<List<NewsSummary>> history(@RequestParam(defaultValue = "") String q,
                                                    Principal principal) {
        long accountId = Long.parseLong(principal.getName());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(newsService.searchHistory(q, accountId));
    }
}
