package com.example.demo.meta_data.infrastructure.persistence.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class MarketCodeKey {
    @Column(name = "exchange_id", nullable = false)
    private Long exchangeId; // FK는 성능 위해 물리 제약 생략(원하면 DDL에서 추가)

    @Column(name = "base", nullable = false, length = 32)
    private String baseAsset;

    @Column(name = "quote", nullable = false, length = 32)
    private String quoteAsset;

    @Column(name = "trading_pair", length = 64)
    private String tradingPair; // 거래소 원시 심볼(BTCUSDT 등)
}
