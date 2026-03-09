package com.example.demo.meta_data.application.port.in;

import com.example.demo.contracts.message.raw.MarketCodeRawMessage;

public interface ConsumeMetaRawUseCase {
    void handle(MarketCodeRawMessage raw);
}
