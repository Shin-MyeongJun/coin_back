package com.example.demo.ingestion.exchange.application.port.in;


import com.example.demo.contracts.message.raw.MarketCodeRawMessage;

import java.util.List;

public interface GetMarketCodeRaw {
    List<MarketCodeRawMessage> getAll();
}
