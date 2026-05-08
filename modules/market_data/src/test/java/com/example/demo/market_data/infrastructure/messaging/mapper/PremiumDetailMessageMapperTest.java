package com.example.demo.market_data.infrastructure.messaging.mapper;

import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PremiumDetailMessageMapperTest {

    PremiumDetailMessageMapper sut = new PremiumDetailMessageMapper();

    private static PremiumDetail detail() {
        return new PremiumDetail(
                "ETH", 10L, 30L,
                new BigDecimal("50000000"), new BigDecimal("50100000"), BigDecimal.ONE,
                new BigDecimal("36000"),    new BigDecimal("36100"),    new BigDecimal("0.000725"),
                7_777L
        );
    }

    @Test
    @DisplayName("toMessage — 9개 원시 필드 전부 매핑")
    void toMessage_mapsAllRawFields() {
        // given
        PremiumDetail pd = detail();

        // when
        PremiumDetailMessage msg = sut.toMessage(pd);

        // then
        assertThat(msg.symbol()).isEqualTo("ETH");
        assertThat(msg.baseExchangeId()).isEqualTo(10L);
        assertThat(msg.compareExchangeId()).isEqualTo(30L);
        assertThat(msg.baseBid()).isEqualByComparingTo(pd.baseBid());
        assertThat(msg.baseAsk()).isEqualByComparingTo(pd.baseAsk());
        assertThat(msg.baseQuoteVal()).isEqualByComparingTo(pd.baseQuoteVal());
        assertThat(msg.compareBid()).isEqualByComparingTo(pd.compareBid());
        assertThat(msg.compareAsk()).isEqualByComparingTo(pd.compareAsk());
        assertThat(msg.compareQuoteVal()).isEqualByComparingTo(pd.compareQuoteVal());
        assertThat(msg.timestamp()).isEqualTo(7_777L);
    }

    @Test
    @DisplayName("toDomain — 라운드트립 원시 필드 보존")
    void toDomain_roundtrip_rawFieldsPreserved() {
        // given
        PremiumDetail original = detail();

        // when
        PremiumDetail restored = sut.toDomain(sut.toMessage(original));

        // then — bid()/ask()는 계산 파생값이므로 원시 필드만 비교
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
