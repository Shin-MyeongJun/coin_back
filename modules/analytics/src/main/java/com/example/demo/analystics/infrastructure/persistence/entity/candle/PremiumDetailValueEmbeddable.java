package com.example.demo.analystics.infrastructure.persistence.entity.candle;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumDetailValueEmbeddable {

    @Column(name = "base_price", nullable = false, precision = 30, scale = 10)
    private BigDecimal basePrice;

    @Column(name = "base_quote_val", nullable = false, precision = 30, scale = 10)
    private BigDecimal baseQuoteVal;

    @Column(name = "compare_price", nullable = false, precision = 30, scale = 10)
    private BigDecimal comparePrice;

    @Column(name = "compare_quote_val", nullable = false, precision = 30, scale = 10)
    private BigDecimal compareQuoteVal;
}
