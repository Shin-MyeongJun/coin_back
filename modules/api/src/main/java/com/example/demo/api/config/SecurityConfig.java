package com.example.demo.api.config;

import com.example.demo.api.config.security.SecurityProperties;
import com.example.demo.api.config.security.error.ProblemDetails;
import com.example.demo.api.config.security.filter.ApiKeyAuthenticationFilter;
import com.example.demo.api.config.security.filter.JwtAuthenticationFilter;
import com.example.demo.api.config.security.filter.PrincipalSupport;
import com.example.demo.api.config.security.filter.RateLimitFilter;
import com.example.demo.api.config.security.filter.SseTicketFilter;
import com.example.demo.api.config.security.ratelimit.RateLimiterPort;
import com.example.demo.api.config.security.ssetoken.ConsumeSseTicketPort;
import com.example.demo.user.application.port.in.AuthenticateApiKeyUseCase;
import com.example.demo.user.application.port.in.LoadRateLimitPolicyQuery;
import com.example.demo.user.application.port.in.VerifyAccessTokenUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Clock;

/**
 * api 모듈 Spring Security 구성.
 *
 * <p>필터 체인 순서 (custom 필터는 Spring Security {@link AuthorizationFilter} 직전에 등록):
 * <ol>
 *   <li>{@link SseTicketFilter}              — /api/v1/stream/** + ?t= 가 있을 때만 동작</li>
 *   <li>{@link JwtAuthenticationFilter}      — Authorization: Bearer ...</li>
 *   <li>{@link ApiKeyAuthenticationFilter}   — Authorization: ApiKey ...</li>
 *   <li>{@link RateLimitFilter}              — principal 종류별 token bucket</li>
 *   <li>(Spring) AuthorizationFilter         — request attribute principal 기반 경로 인가</li>
 * </ol>
 *
 * <p>SecurityContextHolder 의존을 최소화하기 위해
 * 인가 결정은 {@link PrincipalSupport} 의 request attribute 만으로 수행한다.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
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
                                           SecurityProperties properties,
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
                    // permitAll: 로그인/회원가입/refresh/sse-ticket 요청
                    auth.requestMatchers(
                            "/api/v1/auth/signup",
                            "/api/v1/auth/login",
                            "/api/v1/auth/refresh"
                    ).permitAll();

                    // sse-ticket 발급: 인증 필요 (JWT/ApiKey)
                    auth.requestMatchers("/api/v1/auth/sse-ticket")
                            .access(new PrincipalAuthorization());

                    // 사용자 본인 자원: 인증 필요
                    auth.requestMatchers(
                            "/api/v1/auth/logout",
                            "/api/v1/auth/me",
                            "/api/v1/api-keys/**"
                    ).access(new PrincipalAuthorization());

                    // SSE stream: ticket 으로만 인증 (필터에서 principal 주입됨)
                    auth.requestMatchers("/api/v1/stream/**")
                            .access(new PrincipalAuthorization());

                    // 기존 read endpoints — public-read 토글
                    String[] readPaths = {
                            "/api/v1/meta/**",
                            "/api/v1/market/**",
                            "/api/v1/analytics/**",
                            "/api/v1/economic/**",
                            "/api/v1/compose/**"
                    };
                    if (properties.publicRead()) {
                        auth.requestMatchers(readPaths).permitAll();
                    } else {
                        auth.requestMatchers(readPaths).access(new PrincipalAuthorization());
                    }

                    // actuator, openapi: 항상 허용 (운영 전 단계 — prod 에서는 별도 차단)
                    auth.requestMatchers(
                            "/actuator/**",
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html"
                    ).permitAll();

                    auth.anyRequest().permitAll();
                })
                // 필터 등록 순서: 마지막에 register 된 것이 chain의 가장 늦은 위치(AuthorizationFilter 직전).
                // before(AuthorizationFilter.class) 로 일관되게 등록하면, 추가 순서가 실제 실행 순서를 결정한다.
                .addFilterBefore(sseTicketFilter, AuthorizationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, SseTicketFilter.class)
                .addFilterAfter(apiKeyAuthenticationFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, ApiKeyAuthenticationFilter.class);

        return http.build();
    }
}
