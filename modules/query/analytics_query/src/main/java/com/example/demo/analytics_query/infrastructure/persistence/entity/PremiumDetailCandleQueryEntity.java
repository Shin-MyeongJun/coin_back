package com.example.demo.analytics_query.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Immutable
@Entity
@Table(name = "premium_detail_candle")
@Getter
@NoArgsConstructor
public class PremiumDetailCandleQueryEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "base_exchange_id", nullable = false)
    private Long baseExchangeId;

    @Column(name = "compare_exchange_id", nullable = false)
    private Long compareExchangeId;

    @Column(name = "interval")
    private String interval;

    @Column(name = "open_base_price")
    private BigDecimal openBasePrice;

    @Column(name = "open_base_quote_val")
    private BigDecimal openBaseQuoteVal;

    @Column(name = "open_compare_price")
    private BigDecimal openComparePrice;

    @Column(name = "open_compare_quote_val")
    private BigDecimal openCompareQuoteVal;

    @Column(name = "high_base_price")
    private BigDecimal highBasePrice;

    @Column(name = "high_base_quote_val")
    private BigDecimal highBaseQuoteVal;

    @Column(name = "high_compare_price")
    private BigDecimal highComparePrice;

    @Column(name = "high_compare_quote_val")
    private BigDecimal highCompareQuoteVal;

    @Column(name = "low_base_price")
    private BigDecimal lowBasePrice;

    @Column(name = "low_base_quote_val")
    private BigDecimal lowBaseQuoteVal;

    @Column(name = "low_compare_price")
    private BigDecimal lowComparePrice;

    @Column(name = "low_compare_quote_val")
    private BigDecimal lowCompareQuoteVal;

    @Column(name = "close_base_price")
    private BigDecimal closeBasePrice;

    @Column(name = "close_base_quote_val")
    private BigDecimal closeBaseQuoteVal;

    @Column(name = "close_compare_price")
    private BigDecimal closeComparePrice;

    @Column(name = "close_compare_quote_val")
    private BigDecimal closeCompareQuoteVal;

    @Column(name = "bucket_open_ts", nullable = false)
    private Long bucketOpenTs;

    @Column(name = "bucket_close_ts", nullable = false)
    private Long bucketCloseTs;

    @Column(name = "observe_open_ts", nullable = false)
    private Long observeOpenTs;

    @Column(name = "observe_close_ts", nullable = false)
    private Long observeCloseTs;
}
