package com.example.demo.ingestion.economic.economic_ind.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "fred")
@Getter @Setter
public class FredProperties {

    private String apiKey;
    private String baseUrl;
    private List<TrackedSeries> trackedSeries;
    private ScheduleConfig schedule;

    @Getter
    @Setter
    public static class TrackedSeries {
        private String standardIndName;
        private String seriesId;
        private Integer releaseId;
        private String name;
        private String frequency;   // DAILY, MONTHLY, QUARTERLY
    }

    @Getter @Setter
    public static class ScheduleConfig {
        private String releaseSyncCron;
        private Long observationCheckMs;
    }
}
