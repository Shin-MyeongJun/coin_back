package com.example.demo.ingestion.economic.crawling.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "crawling")
@Getter
@Setter
public class CrawlingProperties {

    private YahooConfig yahoo = new YahooConfig();
    private InvestingConfig investing = new InvestingConfig();

    @Getter
    @Setter
    public static class YahooConfig {
        private String baseUrl = "https://query1.finance.yahoo.com";
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        private long pollingIntervalMs = 2000;
        private List<String> symbols = List.of();
    }

    @Getter
    @Setter
    public static class InvestingConfig {
        private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        private long pollingIntervalMs = 5000;
        private List<InstrumentConfig> instruments = List.of();

        @Getter
        @Setter
        public static class InstrumentConfig {
            private String symbol;
            private String name;
            private String url;
        }
    }
}
