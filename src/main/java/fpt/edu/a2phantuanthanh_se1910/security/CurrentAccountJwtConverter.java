// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.security;

import fpt.edu.a2phantuanthanh_se1910.dto.response.AccountResponse;
import fpt.edu.a2phantuanthanh_se1910.service.AuthService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CurrentAccountJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final AuthService authService;

    public CurrentAccountJwtConverter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        long accountId;
        try {
            accountId = Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new InvalidBearerTokenException("Invalid account identity");
        }
        AccountResponse account = authService.findCurrentAccount(accountId);
        String authority;
        if (account.accountRole() == 1) {
            authority = "ROLE_ADMIN";
        } else if (account.accountRole() == 2) {
            authority = "ROLE_STAFF";
        } else {
            throw new InvalidBearerTokenException("Invalid account role");
        }
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority(authority)),
                account.accountId().toString());
    }
}
