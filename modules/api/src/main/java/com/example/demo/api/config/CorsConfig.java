package com.example.demo.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 구성.
 *
 * <p>{@code api.cors.allowed-origins} (env: {@code API_CORS_ALLOWED_ORIGINS}, 콤마 구분) 가
 * 우선이며, 미지정 시 dev 폴백으로 {@code http://localhost:5173, http://localhost:3000} 만 허용한다.
 * {@code allowCredentials=true} 이므로 와일드카드 {@code *} 는 의도적으로 지원하지 않는다.
 */
@Configuration
public class CorsConfig {

    private static final List<String> DEV_FALLBACK_ORIGINS = List.of(
            "http://localhost:5173",
            "http://localhost:3000"
    );

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${api.cors.allowed-origins:}") String allowedOriginsRaw) {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(parseOrigins(allowedOriginsRaw));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Retry-After", "X-Request-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static List<String> parseOrigins(String raw) {
        if (raw == null || raw.isBlank()) {
            return DEV_FALLBACK_ORIGINS;
        }
        List<String> parsed = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        return parsed.isEmpty() ? DEV_FALLBACK_ORIGINS : parsed;
    }
}
