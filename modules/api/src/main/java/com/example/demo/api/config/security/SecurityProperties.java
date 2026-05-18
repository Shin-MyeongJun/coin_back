package com.example.demo.api.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * {@code ys.security.*} 설정 바인딩.
 *
 * <ul>
 *   <li>{@code publicRead} — true(MVP 호환): 기존 조회 endpoint 익명 허용 / false(prod): 인증 필수</li>
 *   <li>{@code cors.allowedOrigins} — CORS allowlist</li>
 *   <li>{@code rateLimit.anonymousRpm} — 익명 fallback 분당 한도(필터에서 직접 @Value)</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "ys.security")
public record SecurityProperties(
        boolean publicRead,
        Cors cors,
        RateLimit rateLimit
) {

    public SecurityProperties {
        if (cors == null) cors = new Cors(List.of("http://localhost:5173"));
        if (rateLimit == null) rateLimit = new RateLimit(30);
    }

    public record Cors(List<String> allowedOrigins) {
        public Cors {
            if (allowedOrigins == null || allowedOrigins.isEmpty()) {
                allowedOrigins = List.of("http://localhost:5173");
            }
        }
    }

    public record RateLimit(int anonymousRpm) {
        public RateLimit {
            if (anonymousRpm <= 0) anonymousRpm = 30;
        }
    }
}
