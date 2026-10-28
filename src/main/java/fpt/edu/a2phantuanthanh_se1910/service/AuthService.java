// Author: Phan Tuan Thanh - CE190595
package fpt.edu.a2phantuanthanh_se1910.service;

import fpt.edu.a2phantuanthanh_se1910.dto.request.LoginRequest;
import fpt.edu.a2phantuanthanh_se1910.dto.response.AccountResponse;
import fpt.edu.a2phantuanthanh_se1910.dto.response.LoginResponse;
import fpt.edu.a2phantuanthanh_se1910.entity.SystemAccount;
import fpt.edu.a2phantuanthanh_se1910.repository.SystemAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final SystemAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    private final Duration lifetime;
    private final String dummyPasswordHash;

    public AuthService(SystemAccountRepository accounts, PasswordEncoder passwordEncoder, JwtEncoder jwtEncoder,
                       @Value("${app.jwt.issuer}") String issuer, @Value("${app.jwt.lifetime}") Duration lifetime) {
        if (lifetime.isNegative() || lifetime.isZero()) {
            throw new IllegalArgumentException("JWT lifetime must be positive");
        }
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.lifetime = lifetime;
        this.dummyPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    public LoginResponse login(LoginRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes");
        }
        String email = request.email().toLowerCase(Locale.ROOT);
        Optional<SystemAccount> result = accounts.findByAccountEmailIgnoreCase(email);
        String passwordHash = dummyPasswordHash;
        if (result.isPresent()) {
            passwordHash = result.get().getAccountPassword();
        }
        boolean passwordMatches = passwordEncoder.matches(request.password(), passwordHash);
        if (result.isEmpty() || !passwordMatches) {
            throw new BadCredentialsException("Invalid credentials");
        }
        SystemAccount account = result.get();
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(lifetime);
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer)
                .subject(account.getAccountId().toString()).issuedAt(issuedAt).expiresAt(expiresAt).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", expiresAt, mapAccount(account));
    }

    public AccountResponse findCurrentAccount(long accountId) {
        SystemAccount account = accounts.findById(accountId)
                .orElseThrow(() -> new InvalidBearerTokenException("Account no longer exists"));
        return mapAccount(account);
    }

    private AccountResponse mapAccount(SystemAccount account) {
        return new AccountResponse(account.getAccountId(), account.getAccountName(),
                account.getAccountEmail(), account.getAccountRole());
    }
}
