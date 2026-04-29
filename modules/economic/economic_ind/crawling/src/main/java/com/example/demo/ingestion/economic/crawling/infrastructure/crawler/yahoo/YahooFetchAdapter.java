package com.example.demo.ingestion.economic.crawling.infrastructure.crawler.yahoo;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.crawling.application.port.out.FetchGlobalIndexPort;
import com.example.demo.ingestion.economic.crawling.domain.domain.GlobalIndex;
import com.example.demo.ingestion.economic.crawling.infrastructure.crawler.yahoo.dto.YahooQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class YahooFetchAdapter implements FetchGlobalIndexPort.ForYahoo {

    private final YahooFinanceClient client;
    private final RawToDomain<YahooQuote, GlobalIndex> mapper;

    @Override
    public List<GlobalIndex> fetchAll() {
        return client.fetchQuotes().stream()
                .map(quote -> mapper.toDomain(quote, Map.of()))
                .toList();
    }
}
