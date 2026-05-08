package com.example.demo.market_data.application.usecase.batch_save;

import com.example.demo.market_data.application.port.out.WritePriceValuePort;
import com.example.demo.market_data.application.port.out.WriteRedisLatestDataPort;
import com.example.demo.market_data.domain.buffer.PremiumBuffer;
import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.domain.service.PremiumKeyParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PersistPremiumBatchUseCaseTest {

    @Mock WritePriceValuePort<Premium> dbAdapter;
    @Mock WriteRedisLatestDataPort<Premium> redisAdapter;

    // PremiumBuffer는 순수 컬렉션 보관자 — 실제 객체 주입
    PremiumBuffer buffer;

    PersistPremiumBatchUseCase sut;

    @BeforeEach
    void setUp() {
        buffer = new PremiumBuffer(new PremiumKeyParser());
        sut = new PersistPremiumBatchUseCase(dbAdapter, redisAdapter, buffer);
    }

    private static Premium premium(String symbol, long baseId, long compareId) {
        return new Premium(symbol, baseId, compareId,
                new BigDecimal("1.5"), new BigDecimal("1.6"), 1_000L);
    }

    @Test
    @DisplayName("flush — buffer drain → dbWriter.saveAll → redisWriter.upsertAll 순서 보장")
    void flush_callsDbThenRedis_inOrder() {
        // given
        buffer.add(premium("BTC", 10L, 20L));
        buffer.add(premium("ETH", 10L, 30L));
        InOrder order = inOrder(dbAdapter, redisAdapter);

        // when
        sut.flush();

        // then
        order.verify(dbAdapter).saveAll(anyList());
        order.verify(redisAdapter).upsertAll(anyList());
    }

    @Test
    @DisplayName("flush — buffer에 담긴 항목이 db·redis에 전달됨")
    void flush_passesBufferedItemsToAdapters() {
        // given
        buffer.add(premium("BTC", 10L, 20L));
        buffer.add(premium("ETH", 10L, 30L));

        // when
        sut.flush();

        // then
        then(dbAdapter).should().saveAll(anyList());
        then(redisAdapter).should().upsertAll(anyList());
    }

    @Test
    @DisplayName("flush — 빈 버퍼: db·redis 모두 빈 리스트로 호출됨")
    void flush_emptyBuffer_callsAdaptersWithEmptyList() {
        // given — buffer에 아무것도 add 안 함

        // when
        sut.flush();

        // then — PersistPriceValueBatchUseCase는 empty 체크 없이 위임
        then(dbAdapter).should().saveAll(List.of());
        then(redisAdapter).should().upsertAll(List.of());
    }

    @Test
    @DisplayName("flush — db 예외 시 redis 미호출(예외 전파)")
    void flush_dbThrows_redisNotCalled() {
        // given
        buffer.add(premium("BTC", 10L, 20L));
        willThrow(new RuntimeException("DB error")).given(dbAdapter).saveAll(anyList());

        // when
        assertThrows(RuntimeException.class, () -> sut.flush());

        // then
        then(redisAdapter).should(never()).upsertAll(anyList());
    }
}
