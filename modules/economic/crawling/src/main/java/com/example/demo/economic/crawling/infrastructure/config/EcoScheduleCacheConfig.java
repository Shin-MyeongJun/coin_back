package com.example.demo.economic.crawling.infrastructure.config;

import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoScheduleCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EcoScheduleCacheConfig {

    @Bean
    public EcoScheduleCache ecoScheduleCache() {
        return new EcoScheduleCache();
    }
}
