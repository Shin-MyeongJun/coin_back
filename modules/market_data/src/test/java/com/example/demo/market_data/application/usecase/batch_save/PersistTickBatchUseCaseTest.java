package com.example.demo.market_data.application.usecase.batch_save;

import com.example.demo.market_data.application.port.out.WritePriceValuePort;
import com.example.demo.market_data.application.port.out.WriteRedisLatestDataPort;
import com.example.demo.market_data.domain.buffer.TickBuffer;
import com.example.demo.market_data.domain.domain.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PersistTickBatchUseCaseTest {

    @Mock WritePriceValuePort<Tick> dbAdapter;
    @Mock WriteRedisLatestDataPort<Tick> redisAdapter;

    // TickBuffer는 단순 컬렉션 보관자 — 실제 객체 주입
    TickBuffer buffer;

    PersistTickBatchUseCase sut;

    @BeforeEach
    void setUp() {
        buffer = new TickBuffer();
        sut = new PersistTickBatchUseCase(dbAdapter, redisAdapter, buffer);
    }

    private static Tick tick(long id, String bid, String ask) {
        return new Tick(id, new BigDecimal(bid), new BigDecimal(ask), 1_000L);
    }

    @Test
    @DisplayName("flush — buffer drain → dbWriter.saveAll → redisWriter.upsertAll 순서 보장")
    void flush_callsDbThenRedis_inOrder() {
        // given
        buffer.add(tick(1L, "50000", "50100"));
        buffer.add(tick(2L, "36000", "36100"));
        InOrder order = inOrder(dbAdapter, redisAdapter);

        // when
        sut.flush();

        // then
        order.verify(dbAdapter).saveAll(anyList());
        order.verify(redisAdapter).upsertAll(anyList());
    }

    @Test
    @DisplayName("flush — dbWriter에 buffer 항목 전달 검증")
    void flush_passesBufferedItemsToDb() {
        // given
        Tick t1 = tick(1L, "50000", "50100");
        Tick t2 = tick(2L, "36000", "36100");
        buffer.add(t1);
        buffer.add(t2);

        // when
        sut.flush();

        // then
        then(dbAdapter).should().saveAll(anyList());
        then(redisAdapter).should().upsertAll(anyList());
    }

    @Test
    @DisplayName("flush — 빈 버퍼: db·redis 모두 빈 리스트로 호출됨(TickBuffer no-guard)")
    void flush_emptyBuffer_callsAdaptersWithEmptyList() {
        // given — buffer에 아무것도 add 안 함

        // when
        sut.flush();

        // then — PersistPriceValueBatchUseCase는 empty 체크 없이 위임함
        then(dbAdapter).should().saveAll(List.of());
        then(redisAdapter).should().upsertAll(List.of());
    }

    @Test
    @DisplayName("flush — db 예외 시 redis 호출 여부는 예외 전파에 달림(현재: 예외 전파)")
    void flush_dbThrows_redisNotCalled() {
        // given
        buffer.add(tick(1L, "50000", "50100"));
        given(dbAdapter.saveAll(anyList())).willThrow(new RuntimeException("DB error"));

        // when
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> sut.flush());

        // then — db 실패 시 redis 호출 안 됨
        then(redisAdapter).should(never()).upsertAll(anyList());
    }
}
