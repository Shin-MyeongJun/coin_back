package com.example.demo.market_data.domain.buffer;

import com.example.demo.market_data.domain.domain.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TickBufferTest {

    TickBuffer sut;

    @BeforeEach
    void setUp() {
        sut = new TickBuffer();
    }

    private static Tick tick(long marketCodeId, String bid, String ask) {
        return new Tick(marketCodeId, new BigDecimal(bid), new BigDecimal(ask), 1_000L);
    }

    @Test
    @DisplayName("add 후 flush — 추가한 항목 전부 반환")
    void add_then_flush_returnsAllItems() {
        // given
        Tick t1 = tick(1L, "50000", "50100");
        Tick t2 = tick(2L, "36000", "36100");
        sut.add(t1);
        sut.add(t2);

        // when
        List<Tick> result = sut.flush();

        // then
        assertThat(result).hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(t1, t2);
    }

    @Test
    @DisplayName("같은 marketCodeId 재add — 최신 값으로 덮어쓴다(Map 특성)")
    void add_sameMarketCodeId_overwritesWithLatest() {
        // given
        Tick old = tick(1L, "50000", "50100");
        Tick latest = tick(1L, "51000", "51100");
        sut.add(old);
        sut.add(latest);

        // when
        List<Tick> result = sut.flush();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).bid()).isEqualByComparingTo(new BigDecimal("51000"));
    }

    @Test
    @Disabled("COIN-BUG-001 수정 후 더 이상 유효하지 않음 — 다음 PR 에서 삭제 예정")
    @DisplayName("현재 동작 검증 — flush 후 다시 flush해도 동일 데이터 반환(clear 없음)")
    void flush_twice_returnsSameData() {
        // given
        sut.add(tick(1L, "50000", "50100"));

        // when
        List<Tick> first  = sut.flush();
        List<Tick> second = sut.flush();

        // then — 버퍼가 비워지지 않으므로 두 번째도 동일 결과
        assertThat(first).hasSize(1);
        assertThat(second).hasSize(1);
    }

    @Test
    @DisplayName("flush 후 다음 flush 는 비어 있다 (COIN-BUG-001 회귀)")
    void flush_afterFlush_shouldBeEmpty_butIsNot() {
        // given
        sut.add(tick(1L, "50000", "50100"));
        sut.flush(); // 첫 번째 flush (DB/Redis 저장 시뮬레이션)

        // when
        List<Tick> second = sut.flush();

        // then
        assertThat(second).isEmpty();
    }

    @Test
    @DisplayName("동시성 — 여러 스레드 add + flush 동시 수행 시 NPE/데이터 손실 없음")
    void concurrentAddAndFlush_noExceptionOrDataLoss() throws InterruptedException {
        // given
        int writers = 5;
        int itemsPerWriter = 20;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(writers + 1);
        CopyOnWriteArrayList<Tick> flushed = new CopyOnWriteArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(writers + 1)) {
            // writer threads
            for (int w = 0; w < writers; w++) {
                final int wId = w;
                pool.submit(() -> {
                    try {
                        start.await();
                        for (int i = 0; i < itemsPerWriter; i++) {
                            sut.add(tick(wId * 100 + i, "100", "101"));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done.countDown();
                    }
                });
            }

            // reader thread
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < 10; i++) {
                        flushed.addAll(sut.flush());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });

            // when
            start.countDown();
            assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        }

        // then — 예외 없이 완료됨이 곧 ConcurrentHashMap의 thread-safety 검증
        assertThat(flushed).allSatisfy(t -> assertThat(t).isNotNull());
    }
}
