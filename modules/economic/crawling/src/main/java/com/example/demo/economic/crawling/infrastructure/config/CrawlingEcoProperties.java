package com.example.demo.economic.crawling.infrastructure.config;

import com.example.demo.ingestion.economic.economic_ind.domain.enums.IndicatorUnit;
import com.example.demo.ingestion.economic.economic_ind.domain.enums.ReleaseFrequency;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "crawling-eco")
@Getter
@Setter
public class CrawlingEcoProperties {

    private BurstConfig burst = new BurstConfig();
    private List<IndicatorConfig> indicators = new ArrayList<>();

    public IndicatorConfig findByType(String type) {
        if (type == null) return null;
        return indicators.stream()
                .filter(i -> type.equalsIgnoreCase(i.getType()))
                .findFirst()
                .orElse(null);
    }

    @Getter
    @Setter
    public static class BurstConfig {
        private long intervalMs = 30_000;
        private long timeoutMs = 7_200_000;
    }

    @Getter
    @Setter
    public static class IndicatorConfig {
        private String type;
        private String country;
        private ReleaseFrequency frequency;
        private IndicatorUnit unit;
        private String investingEventName;
        private String yahooEventName;
    }
}
