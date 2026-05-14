package com.example.demo.user.infrastructure.web;

import com.example.demo.user.application.port.in.AuthenticatedAccount;
import com.example.demo.user.application.port.in.LoginUseCase;
import com.example.demo.user.application.port.in.LogoutUseCase;
import com.example.demo.user.application.port.in.RefreshTokenUseCase;
import com.example.demo.user.application.port.in.SignupUseCase;
import com.example.demo.user.application.port.in.VerifyAccessTokenUseCase;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.Email;
import com.example.demo.user.domain.domain.RefreshToken;
import com.example.demo.user.domain.domain.TokenPair;
import com.example.demo.user.domain.exception.TokenInvalidException;
import com.example.demo.user.infrastructure.security.JwtProperties;
import com.example.demo.user.infrastructure.web.dto.LoginRequest;
import com.example.demo.user.infrastructure.web.dto.MeResponse;
import com.example.demo.user.infrastructure.web.dto.SignupRequest;
import com.example.demo.user.infrastructure.web.dto.SignupResponse;
import com.example.demo.user.infrastructure.web.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    static final String REFRESH_COOKIE = "refresh";

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final VerifyAccessTokenUseCase verifyAccessTokenUseCase;
    private final JwtProperties jwtProperties;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        Account account = signupUseCase.signup(Email.of(req.email()), req.password(), Instant.now());
        SignupResponse body = new SignupResponse(
                account.getId().asString(),
                account.getEmail().value(),
                account.getTier().name(),
                account.getCreatedAt().toEpochMilli()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req,
                                               HttpServletRequest httpRequest) {
        TokenPair pair = loginUseCase.login(Email.of(req.email()), req.password(), Instant.now());
        return tokenResponse(pair, httpRequest.isSecure());
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshCookie,
            HttpServletRequest httpRequest
    ) {
        if (refreshCookie == null || refreshCookie.isBlank()) {
            throw new TokenInvalidException("missing refresh cookie");
        }
        TokenPair pair = refreshTokenUseCase.refresh(refreshCookie, Instant.now());
        return tokenResponse(pair, httpRequest.isSecure());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshCookie,
            HttpServletRequest httpRequest
    ) {
        String accessRaw = stripBearer(authHeader);
        logoutUseCase.logout(accessRaw, refreshCookie, Instant.now());
        ResponseCookie cleared = clearCookie(httpRequest.isSecure());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .build();
    }

    // TODO Step 4: replace direct header parsing with SecurityContext-derived AuthenticatedAccount.
    @GetMapping("/me")
    public MeResponse me(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        AuthenticatedAccount me = verifyAccessTokenUseCase.verify(authHeader, Instant.now())
                .orElseThrow(() -> new TokenInvalidException("missing or invalid access token"));
        return new MeResponse(me.id().asString(), me.email().value(), me.tier().name());
    }

    // -- helpers --

    private ResponseEntity<TokenResponse> tokenResponse(TokenPair pair, boolean secure) {
        ResponseCookie cookie = refreshCookie(pair.refresh(), secure);
        TokenResponse body = new TokenResponse(
                pair.access().raw(),
                pair.access().expiresAt().toEpochMilli()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    private ResponseCookie refreshCookie(RefreshToken refresh, boolean secure) {
        return ResponseCookie.from(REFRESH_COOKIE, refresh.raw())
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(jwtProperties.refreshTtlSeconds())
                .build();
    }

    private ResponseCookie clearCookie(boolean secure) {
        return ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
    }

    private String stripBearer(String header) {
        if (header == null || header.isBlank()) return null;
        String trimmed = header.trim();
        return trimmed.startsWith("Bearer ") ? trimmed.substring("Bearer ".length()).trim() : trimmed;
    }
}
