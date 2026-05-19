package com.example.demo.api.config;

import com.example.demo.api.config.security.error.ProblemDetails;
import com.example.demo.api.config.security.filter.ApiKeyAuthenticationFilter;
import com.example.demo.api.config.security.filter.JwtAuthenticationFilter;
import com.example.demo.api.config.security.filter.RateLimitFilter;
import com.example.demo.api.config.security.filter.SseTicketFilter;
import com.example.demo.api.config.security.ratelimit.RateLimiterPort;
import com.example.demo.api.config.security.ssetoken.ConsumeSseTicketPort;
import com.example.demo.user.application.port.in.AuthenticateApiKeyUseCase;
import com.example.demo.user.application.port.in.LoadRateLimitPolicyQuery;
import com.example.demo.user.application.port.in.VerifyAccessTokenUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Clock;

/**
 * api 모듈 Spring Security 구성.
 *
 * <p>필터 체인 순서 (모두 {@link UsernamePasswordAuthenticationFilter} 앞에 등록):
 * <ol>
 *   <li>{@link SseTicketFilter}              — /api/v1/stream/** + ?t= 가 있을 때만 동작</li>
 *   <li>{@link JwtAuthenticationFilter}      — Authorization: Bearer ... (SSE 경로는 ?access_token= 쿼리 fallback)</li>
 *   <li>{@link ApiKeyAuthenticationFilter}   — Authorization: ApiKey ...</li>
 *   <li>{@link RateLimitFilter}              — principal 종류별 token bucket</li>
 *   <li>(Spring) UsernamePasswordAuthenticationFilter 이후 → AuthorizationFilter 가 SecurityContext 기반 인가 결정</li>
 * </ol>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SseTicketFilter sseTicketFilter(ConsumeSseTicketPort consumeSseTicketPort,
                                           ObjectMapper objectMapper) {
        return new SseTicketFilter(consumeSseTicketPort, objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(VerifyAccessTokenUseCase verify,
                                                            Clock clock,
                                                            ObjectMapper objectMapper) {
        return new JwtAuthenticationFilter(verify, clock, objectMapper);
    }

    @Bean
    public ApiKeyAuthenticationFilter apiKeyAuthenticationFilter(AuthenticateApiKeyUseCase auth,
                                                                  Clock clock,
                                                                  ObjectMapper objectMapper) {
        return new ApiKeyAuthenticationFilter(auth, clock, objectMapper);
    }

    @Bean
    public RateLimitFilter rateLimitFilter(RateLimiterPort rateLimiterPort,
                                            LoadRateLimitPolicyQuery loadRateLimitPolicyQuery,
                                            Clock clock,
                                            ObjectMapper objectMapper) {
        return new RateLimitFilter(rateLimiterPort, loadRateLimitPolicyQuery, clock, objectMapper);
    }

    @Bean
    public AuthenticationEntryPoint problemDetailEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) ->
                ProblemDetails.write(response, objectMapper, HttpStatus.UNAUTHORIZED,
                        "Authentication required",
                        authException == null ? null : authException.getMessage());
    }

    @Bean
    public AccessDeniedHandler problemDetailAccessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, accessDeniedException) ->
                ProblemDetails.write(response, objectMapper, HttpStatus.FORBIDDEN,
                        "Forbidden",
                        accessDeniedException == null ? null : accessDeniedException.getMessage());
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CorsConfigurationSource corsConfigurationSource,
                                           SseTicketFilter sseTicketFilter,
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
                                           RateLimitFilter rateLimitFilter,
                                           AuthenticationEntryPoint entryPoint,
                                           AccessDeniedHandler accessDeniedHandler) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> {
                    // public read endpoints (GET)
                    auth.requestMatchers(HttpMethod.GET,
                            "/api/v1/meta/**",
                            "/api/v1/market/**",
                            "/api/v1/analytics/**",
                            "/api/v1/economic/**",
                            "/api/v1/compose/**",
                            "/api/v1/stream/**"
                    ).permitAll();

                    // auth bootstrap (POST)
                    auth.requestMatchers(HttpMethod.POST,
                            "/api/v1/auth/signup",
                            "/api/v1/auth/login",
                            "/api/v1/auth/refresh"
                    ).permitAll();

                    // ops endpoints
                    auth.requestMatchers(
                            "/actuator/health",
                            "/actuator/prometheus"
                    ).permitAll();

                    // admin
                    auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");

                    // authenticated user-scope endpoints
                    auth.requestMatchers(
                            "/api/v1/auth/me",
                            "/api/v1/auth/logout",
                            "/api/v1/auth/sse-ticket",
                            "/api/v1/api-keys/**",
                            "/api/v1/watchlist/**",
                            "/api/v1/alert/**"
                    ).authenticated();

                    auth.anyRequest().authenticated();
                })
                // 모든 custom 필터는 UsernamePasswordAuthenticationFilter 앞에 등록.
                // 추가 순서가 실제 실행 순서를 결정한다.
                .addFilterBefore(sseTicketFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, SseTicketFilter.class)
                .addFilterAfter(apiKeyAuthenticationFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, ApiKeyAuthenticationFilter.class);

        return http.build();
    }
}
