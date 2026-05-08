package com.example.demo.market_data.infrastructure.persistence.mapper;

import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.infrastructure.persistence.entity.PremiumDetailEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumDetailEntityMapperTest {

    PremiumDetailEntityMapper sut = new PremiumDetailEntityMapper();

    private static PremiumDetail detail() {
        return new PremiumDetail(
                "ETH", 10L, 30L,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"),    new BigDecimal("36100"),    new BigDecimal("0.000725"),
                7_777L
        );
    }

    @Test
    @DisplayName("toEntity — 9개 원시 필드 전부 Entity에 매핑")
    void toEntity_mapsAllRawFields() {
        // given
        PremiumDetail pd = detail();

        // when
        PremiumDetailEntity entity = sut.toEntity(pd);

        // then
        assertThat(entity.getSymbol()).isEqualTo("ETH");
        assertThat(entity.getBaseExchangeId()).isEqualTo(10L);
        assertThat(entity.getCompareExchangeId()).isEqualTo(30L);
        assertThat(entity.getBaseBid()).isEqualByComparingTo(pd.baseBid());
        assertThat(entity.getBaseAsk()).isEqualByComparingTo(pd.baseAsk());
        assertThat(entity.getBaseQuoteVal()).isEqualByComparingTo(pd.baseQuoteVal());
        assertThat(entity.getCompareBid()).isEqualByComparingTo(pd.compareBid());
        assertThat(entity.getCompareAsk()).isEqualByComparingTo(pd.compareAsk());
        assertThat(entity.getCompareQuoteVal()).isEqualByComparingTo(pd.compareQuoteVal());
        assertThat(entity.getTimestamp()).isEqualTo(7_777L);
        assertThat(entity.getId()).isNull();
    }

    @Test
    @DisplayName("toDomain — Entity → Domain 라운드트립 원시 필드 보존")
    void toDomain_roundtrip_rawFieldsPreserved() {
        // given
        PremiumDetail original = detail();

        // when
        PremiumDetail restored = sut.toDomain(sut.toEntity(original));

        // then
        assertThat(restored.symbol()).isEqualTo(original.symbol());
        assertThat(restored.baseBid()).isEqualByComparingTo(original.baseBid());
        assertThat(restored.baseAsk()).isEqualByComparingTo(original.baseAsk());
        assertThat(restored.baseQuoteVal()).isEqualByComparingTo(original.baseQuoteVal());
        assertThat(restored.compareBid()).isEqualByComparingTo(original.compareBid());
        assertThat(restored.compareAsk()).isEqualByComparingTo(original.compareAsk());
        assertThat(restored.compareQuoteVal()).isEqualByComparingTo(original.compareQuoteVal());
        assertThat(restored.timestamp()).isEqualTo(original.timestamp());
    }
}
