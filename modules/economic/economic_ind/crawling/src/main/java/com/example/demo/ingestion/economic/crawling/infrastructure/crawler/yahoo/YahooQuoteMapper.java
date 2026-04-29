package com.example.demo.ingestion.economic.crawling.infrastructure.crawler.yahoo;

import com.example.demo.infra_shard.messaging.mapper.RawToDomain;
import com.example.demo.ingestion.economic.crawling.domain.domain.GlobalIndex;
import com.example.demo.ingestion.economic.crawling.infrastructure.crawler.yahoo.dto.YahooQuote;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class YahooQuoteMapper implements RawToDomain<YahooQuote, GlobalIndex> {

    @Override
    public GlobalIndex toDomain(YahooQuote raw, Map<String, String> args) {
        String name = raw.longName() != null ? raw.longName()
                : (raw.shortName() != null ? raw.shortName() : raw.symbol());
        long timestamp = raw.regularMarketTime() > 0
                ? raw.regularMarketTime() * 1000L
                : System.currentTimeMillis();
        return new GlobalIndex(
                raw.symbol(),
                name,
                BigDecimal.valueOf(raw.regularMarketPrice()),
                timestamp,
                "yahoo"
        );
    }
}
