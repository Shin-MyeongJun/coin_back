package com.example.demo.ingestion.exchange.application.port.in;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;
import com.example.demo.ingestion.exchange.domain.MarketCodeKey;
import com.example.demo.ingestion.exchange.domain.MarketCodeValue;

public interface ParseMarketCodeRawUseCase {
    MarketCodeKey getKey(MarketCodeRawMessage ms);
    MarketCodeValue getValue(MarketCodeRawMessage ms);
}
