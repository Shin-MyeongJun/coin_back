package com.example.demo.ingestion.economic.crawling.infrastructure.crawler.investing;

import com.example.demo.ingestion.economic.crawling.application.port.out.FetchGlobalIndexPort;
import com.example.demo.ingestion.economic.crawling.domain.domain.GlobalIndex;
import com.example.demo.ingestion.economic.crawling.infrastructure.config.CrawlingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InvestingFetchAdapter implements FetchGlobalIndexPort.ForInvesting {

    private final InvestingClient client;
    private final CrawlingProperties properties;

    @Override
    public List<GlobalIndex> fetchAll() {
        List<GlobalIndex> results = new ArrayList<>();
        for (CrawlingProperties.InvestingConfig.InstrumentConfig inst : properties.getInvesting().getInstruments()) {
            Optional<BigDecimal> price = client.scrapePrice(inst.getUrl());
            price.ifPresent(p -> results.add(new GlobalIndex(
                    inst.getSymbol(),
                    inst.getName(),
                    p,
                    System.currentTimeMillis(),
                    "investing"
            )));
        }
        return results;
    }
}
