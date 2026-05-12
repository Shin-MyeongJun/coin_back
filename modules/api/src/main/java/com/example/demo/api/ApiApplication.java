package com.example.demo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example.demo.api",
        "com.example.demo.analytics_query",
        "com.example.demo.meta_data_query",
        "com.example.demo.market_data_query",
        "com.example.demo.economic_query",
        "com.example.demo.infra_shard"
})
@EntityScan(basePackages = {
        "com.example.demo.analytics_query.infrastructure.persistence.entity",
        "com.example.demo.meta_data_query.infrastructure.persistence.entity",
        "com.example.demo.market_data_query.infrastructure.persistence.entity",
        "com.example.demo.economic_query.infrastructure.persistence.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.example.demo.analytics_query.infrastructure.persistence.repo",
        "com.example.demo.meta_data_query.infrastructure.persistence.repo",
        "com.example.demo.market_data_query.infrastructure.persistence.repo",
        "com.example.demo.economic_query.infrastructure.persistence.repo"
})
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
