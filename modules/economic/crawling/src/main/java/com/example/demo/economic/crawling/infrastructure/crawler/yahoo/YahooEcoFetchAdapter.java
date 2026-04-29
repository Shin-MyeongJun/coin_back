package com.example.demo.economic.crawling.infrastructure.crawler.yahoo;

import com.example.demo.economic.crawling.application.port.out.FetchEcoValuePort;
import com.example.demo.economic.crawling.infrastructure.config.CrawlingEcoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class YahooEcoFetchAdapter implements FetchEcoValuePort.ForYahoo {

    private final YahooEcoClient client;
    private final CrawlingEcoProperties properties;

    @Override
    public Optional<BigDecimal> fetchCurrentValue(String indicatorType) {
        CrawlingEcoProperties.IndicatorConfig config = properties.findByType(indicatorType);
        if (config == null || config.getYahooEventName() == null) {
            return Optional.empty();
        }
        return client.fetchActualValue(config.getYahooEventName());
    }
}
