package com.example.demo.analystics.infrastructure.persistence.entity.candle;


import com.example.demo.analystics.domain.domain.Interval;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "premium_detail_candle"
)
@SequenceGenerator(
        name = "premium_detail_candle_seq_gen",
        sequenceName = "premium_detail_candle_seq",
        allocationSize = 1000   // ← 하이버네이트가 1000개씩 ID 블록을 미리 가져와 round-trip을 줄임
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PremiumDetailCandleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator ="premium_detail_candle_seq_gen" )
    private Long id;
    @Column
    private String symbol;

    @Column(name = "base_exchange_id", nullable = false)
    private Long baseExchangeId;

    @Column(name = "compare_exchange_id", nullable = false)
    private Long compareExchangeId;

    @Column
    @Enumerated(EnumType.STRING)
    private Interval interval;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "basePrice", column = @Column(name = "open_base_price")),
            @AttributeOverride(name = "baseQuoteVal", column = @Column(name = "open_base_quote_val")),
            @AttributeOverride(name = "comparePrice", column = @Column(name = "open_compare_price")),
            @AttributeOverride(name = "compareQuoteVal", column = @Column(name = "open_compare_quote_val"))
    })
    private PremiumDetailValueEmbeddable open;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "basePrice", column = @Column(name = "high_base_price")),
            @AttributeOverride(name = "baseQuoteVal", column = @Column(name = "high_base_quote_val")),
            @AttributeOverride(name = "comparePrice", column = @Column(name = "high_compare_price")),
            @AttributeOverride(name = "compareQuoteVal", column = @Column(name = "high_compare_quote_val"))
    })
    private PremiumDetailValueEmbeddable high;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "basePrice", column = @Column(name = "low_base_price")),
            @AttributeOverride(name = "baseQuoteVal", column = @Column(name = "low_base_quote_val")),
            @AttributeOverride(name = "comparePrice", column = @Column(name = "low_compare_price")),
            @AttributeOverride(name = "compareQuoteVal", column = @Column(name = "low_compare_quote_val"))
    })
    private PremiumDetailValueEmbeddable low;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "basePrice", column = @Column(name = "close_base_price")),
            @AttributeOverride(name = "baseQuoteVal", column = @Column(name = "close_base_quote_val")),
            @AttributeOverride(name = "comparePrice", column = @Column(name = "close_compare_price")),
            @AttributeOverride(name = "compareQuoteVal", column = @Column(name = "close_compare_quote_val"))
    })
    private PremiumDetailValueEmbeddable close;

    @Column(nullable = false)
    private Long bucketOpenTs;

    @Column(nullable = false)
    private Long bucketCloseTs;

    @Column(nullable = false)
    private Long observeOpenTs;

    @Column(nullable = false)
    private Long observeCloseTs;
}
