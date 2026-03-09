package com.example.demo.market_data.domain.domain.snapshot;

public record ExchangeSnapShot(
        Long id,
        String name,
        String exchangeType,
        String quote,
        String status
) {
}
