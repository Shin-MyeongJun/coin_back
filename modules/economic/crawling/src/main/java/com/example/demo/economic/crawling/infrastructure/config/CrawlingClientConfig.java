package com.example.demo.economic.crawling.infrastructure.config;

import com.example.demo.economic.crawling.infrastructure.crawler.investing.InvestingEcoClient;
import com.example.demo.economic.crawling.infrastructure.crawler.yahoo.YahooEcoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlingClientConfig {

    @Bean
    public InvestingEcoClient investingEcoClient() {
        return new InvestingEcoClient();
    }

    @Bean
    public YahooEcoClient yahooEcoClient() {
        return new YahooEcoClient();
    }
}
