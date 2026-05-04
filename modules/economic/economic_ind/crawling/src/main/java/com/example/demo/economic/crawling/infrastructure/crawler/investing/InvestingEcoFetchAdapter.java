package com.example.demo.economic.crawling.infrastructure.crawler.investing;

import com.example.demo.economic.crawling.application.port.out.FetchEcoValuePort;
import com.example.demo.economic.crawling.infrastructure.config.CrawlingEcoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InvestingEcoFetchAdapter implements FetchEcoValuePort.ForInvesting {

    private final InvestingEcoClient client;
    private final CrawlingEcoProperties properties;

    @Override
    public Optional<BigDecimal> fetchCurrentValue(String indicatorType) {
        CrawlingEcoProperties.IndicatorConfig config = properties.findByType(indicatorType);
        if (config == null || config.getInvestingEventName() == null) {
            return Optional.empty();
        }
        return client.fetchActualValue(config.getInvestingEventName());
    }
}
