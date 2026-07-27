package com.example.demo.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * api 모듈 JPA 스캔 구성.
 *
 * <p>{@code @EntityScan}/{@code @EnableJpaRepositories}를 {@link com.example.demo.api.ApiApplication}
 * 에 두면 {@code @WebMvcTest} 슬라이스에서도 JPA 인프라(entityManagerFactory) 가 요구되어
 * 컨텍스트 로드가 실패한다. 본 클래스를 별도 {@code @Configuration} 으로 분리해 두면
 * {@code @WebMvcTest} 가 본 클래스를 기본적으로 제외하므로 슬라이스 테스트가 동작한다.
 */
@Configuration
@EntityScan(basePackages = {
        "com.example.demo.user",
        "com.example.demo.alert",
        "com.example.demo.analytics_query",
        "com.example.demo.market_data_query",
        "com.example.demo.meta_data_query",
        "com.example.demo.economic_query"
})
@EnableJpaRepositories(basePackages = {
        "com.example.demo.user",
        "com.example.demo.alert",
        "com.example.demo.analytics_query",
        "com.example.demo.market_data_query",
        "com.example.demo.meta_data_query",
        "com.example.demo.economic_query"
})
public class JpaConfig {
}
