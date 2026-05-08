package com.example.demo.market_data.application.usecase;

import com.example.demo.market_data.application.port.in.PublishPriceDataUseCase;
import com.example.demo.market_data.application.port.out.GetCacheDataPort;
import com.example.demo.market_data.domain.buffer.base.PriceValueBuffer;
import com.example.demo.market_data.domain.domain.Fx;
import com.example.demo.market_data.domain.domain.FxKey;
import com.example.demo.market_data.domain.domain.Premium;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import com.example.demo.market_data.domain.domain.Tick;
import com.example.demo.market_data.domain.domain.snapshot.ExchangeSnapShotVal;
import com.example.demo.market_data.domain.domain.snapshot.MarketCodeSnapShotVal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CalPremiumManagerTest {

    @Mock PriceValueBuffer<Premium> buffer;
    @Mock PriceValueBuffer<PremiumDetail> detailBuffer;
    @Mock GetCacheDataPort<Long, Tick> tickGetter;
    @Mock GetCacheDataPort<Long, MarketCodeSnapShotVal> codeGetter;
    @Mock GetCacheDataPort<Long, ExchangeSnapShotVal> exchangeGetter;
    @Mock GetCacheDataPort<FxKey, Fx> fxGetter;
    @Mock PublishPriceDataUseCase<Premium> premiumPublisher;
    @Mock PublishPriceDataUseCase<PremiumDetail> premiumDetailPublisher;

    @Captor ArgumentCaptor<Premium> premiumCaptor;
    @Captor ArgumentCaptor<PremiumDetail> premiumDetailCaptor;

    CalPremiumManager sut;

    @BeforeEach
    void setUp() {
        sut = new CalPremiumManager(
                buffer, detailBuffer,
                tickGetter, codeGetter, exchangeGetter, fxGetter,
                premiumPublisher, premiumDetailPublisher
        );
    }

    // ─── 픽스처 ─────────────────────────────────────────────────

    private static Tick tick(long id, String bid, String ask) {
        return new Tick(id, new BigDecimal(bid), new BigDecimal(ask), 1_000L);
    }

    private static MarketCodeSnapShotVal mc(Long exchangeId, String base) {
        return new MarketCodeSnapShotVal(exchangeId, base, base + "-USDT");
    }

    private static ExchangeSnapShotVal ex(String name, String quote) {
        return new ExchangeSnapShotVal(name, "SPOT", quote);
    }

    private static Fx fx(String base, String compare, String val) {
        return new Fx(base, compare, new BigDecimal(val), 1_000L);
    }

    // ─── 조기 종료 분기 ──────────────────────────────────────────

    @Test
    @DisplayName("codeGetter(id) empty → 조기 종료: tickGetter 미호출")
    void cal_codeGetterEmpty_earlyReturn() {
        // given
        given(codeGetter.get(1L)).willReturn(Optional.empty());

        // when
        sut.cal(1L);

        // then
        then(tickGetter).shouldHaveNoInteractions();
        then(buffer).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("tickGetter(base) empty → 조기 종료: buffer 미호출")
    void cal_baseTickGetterEmpty_earlyReturn() {
        // given
        given(codeGetter.get(1L)).willReturn(Optional.of(mc(10L, "BTC")));
        given(tickGetter.get(1L)).willReturn(Optional.empty());

        // when
        sut.cal(1L);

        // then
        then(buffer).shouldHaveNoInteractions();
        then(premiumPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("자기 자신만 존재 → 페어링 없음: premium 미발행")
    void cal_onlySelfInList_noPremium() {
        // given
        given(codeGetter.get(1L)).willReturn(Optional.of(mc(10L, "BTC")));
        given(tickGetter.get(1L)).willReturn(Optional.of(tick(1L, "50000000", "50100000")));

        // when — marketCodeList["BTC"] = {1L} 이므로 페어 없음
        sut.cal(1L);

        // then
        then(buffer).shouldHaveNoInteractions();
        then(premiumPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("handling — codeGetter(A tick) empty → 중단: buffer 미호출")
    void handling_codeGetterA_empty_noPublish() {
        // given — cal(1L) 먼저 등록, cal(2L) 에서 페어링 시도
        given(codeGetter.get(1L)).willReturn(Optional.of(mc(10L, "BTC")));
        given(tickGetter.get(1L)).willReturn(Optional.of(tick(1L, "50000000", "50100000")));
        // cal(2L) 최초 호출(등록) → 정상, handling 내 mcA 조회(2번째 호출) → empty
        given(codeGetter.get(2L))
                .willReturn(Optional.of(mc(20L, "BTC")))
                .willReturn(Optional.empty());
        given(tickGetter.get(2L)).willReturn(Optional.of(tick(2L, "36000", "36100")));

        sut.cal(1L);

        // when
        sut.cal(2L);

        // then
        then(buffer).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("handling — codeGetter(B tick) empty → 중단: buffer 미호출")
    void handling_codeGetterB_empty_noPublish() {
        // given — 등록은 정상, handling 내 mcB(idA) 조회 2번째 empty
        given(codeGetter.get(1L))
                .willReturn(Optional.of(mc(10L, "BTC")))
                .willReturn(Optional.empty()); // handling 내 mcB
        given(codeGetter.get(2L)).willReturn(Optional.of(mc(20L, "BTC")));
        given(tickGetter.get(1L)).willReturn(Optional.of(tick(1L, "50000000", "50100000")));
        given(tickGetter.get(2L)).willReturn(Optional.of(tick(2L, "36000", "36100")));
        // exA 조회까지 가지 않도록 mcB가 먼저 empty 반환
        given(exchangeGetter.get(20L)).willReturn(Optional.of(ex("Binance", "USDT")));

        sut.cal(1L);

        // when
        sut.cal(2L);

        // then
        then(buffer).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("handling — exchangeGetter(A) empty → 중단: buffer 미호출")
    void handling_exchangeGetterA_empty_noPublish() {
        // given
        setupTwoIds(1L, 2L, "BTC");
        given(exchangeGetter.get(20L)).willReturn(Optional.empty()); // exA = exchangeGetter(mcA.exchangeId=20L)

        sut.cal(1L);

        // when
        sut.cal(2L);

        // then
        then(buffer).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("handling — exchangeGetter(B) empty → 중단: buffer 미호출")
    void handling_exchangeGetterB_empty_noPublish() {
        // given
        setupTwoIds(1L, 2L, "BTC");
        given(exchangeGetter.get(20L)).willReturn(Optional.of(ex("Binance", "USDT")));
        given(exchangeGetter.get(10L)).willReturn(Optional.empty()); // exB = exchangeGetter(mcB.exchangeId=10L)

        sut.cal(1L);

        // when
        sut.cal(2L);

        // then
        then(buffer).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("handling — fxGetter empty → 중단: buffer 미호출")
    void handling_fxGetterEmpty_noPublish() {
        // given
        setupTwoIds(1L, 2L, "BTC");
        given(exchangeGetter.get(20L)).willReturn(Optional.of(ex("Binance", "USDT")));
        given(exchangeGetter.get(10L)).willReturn(Optional.of(ex("Upbit", "KRW")));
        given(fxGetter.get(new FxKey("USD", "KRW"))).willReturn(Optional.empty());

        sut.cal(1L);

        // when
        sut.cal(2L);

        // then
        then(buffer).shouldHaveNoInteractions();
    }

    // ─── filtering 분기 ──────────────────────────────────────────

    @Test
    @DisplayName("quoteA == quoteB(동일 통화) → filtering false: 프리미엄 계산 안 함")
    void handling_sameQuote_filteringFalse_noPublish() {
        // given — 두 거래소 모두 KRW 마켓
        setupTwoIds(1L, 2L, "BTC");
        given(exchangeGetter.get(20L)).willReturn(Optional.of(ex("Bithumb", "KRW")));
        given(exchangeGetter.get(10L)).willReturn(Optional.of(ex("Upbit", "KRW")));

        sut.cal(1L);

        // when
        sut.cal(2L);

        // then
        then(buffer).shouldHaveNoInteractions();
        then(premiumPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("quote가 USD/KRW/JPY 아닌 경우(null) → filtering false: 프리미엄 계산 안 함")
    void handling_nonFiatQuote_filteringFalse_noPublish() {
        // given
        setupTwoIds(1L, 2L, "ETH");
        given(exchangeGetter.get(20L)).willReturn(Optional.of(ex("ExA", "BTC"))); // normalize → null
        given(exchangeGetter.get(10L)).willReturn(Optional.of(ex("ExB", "KRW")));

        sut.cal(1L);

        // when
        sut.cal(2L);

        // then
        then(buffer).shouldHaveNoInteractions();
    }

    // ─── 정상 경로 ───────────────────────────────────────────────

    @Test
    @DisplayName("정상 경로 — buffer·detailBuffer·publisher 모두 호출, 인자 symbol/exchangeId 검증")
    void cal_happyPath_allCollaboratorsInvoked() {
        // given
        setupTwoIds(1L, 2L, "BTC");
        given(exchangeGetter.get(20L)).willReturn(Optional.of(ex("Binance", "USDT")));
        given(exchangeGetter.get(10L)).willReturn(Optional.of(ex("Upbit", "KRW")));
        given(fxGetter.get(new FxKey("USD", "KRW")))
                .willReturn(Optional.of(fx("USD", "KRW", "0.000725")));

        sut.cal(1L);

        // when
        sut.cal(2L);

        // then
        then(buffer).should(times(1)).add(premiumCaptor.capture());
        then(detailBuffer).should(times(1)).add(premiumDetailCaptor.capture());
        then(premiumPublisher).should(times(1)).process(premiumCaptor.getValue());
        then(premiumDetailPublisher).should(times(1)).process(premiumDetailCaptor.getValue());

        Premium premium = premiumCaptor.getValue();
        assertThat(premium.symbol()).isEqualTo("BTC");
        assertThat(premium.baseExchangeId()).isEqualTo(20L);   // a(idB=2L) → mc(20L)
        assertThat(premium.compareExchangeId()).isEqualTo(10L); // b(idA=1L) → mc(10L)
    }

    @Test
    @DisplayName("Premium bid/ask 공식 — (compareBid*fxB)/(baseAsk*fxA) - 1) * 100, scale=8 HALF_UP")
    void cal_premiumFormula_bid_ask_correct() {
        // given
        BigDecimal bidA = new BigDecimal("50000000"); // Upbit bid (KRW) — idA=1L
        BigDecimal askA = new BigDecimal("50100000"); // Upbit ask (KRW)
        BigDecimal bidB = new BigDecimal("36000");    // Binance bid (USD) — idB=2L
        BigDecimal askB = new BigDecimal("36100");    // Binance ask (USD)
        BigDecimal fxVal = new BigDecimal("0.000725"); // 1 KRW = 0.000725 USD

        given(codeGetter.get(1L)).willReturn(Optional.of(mc(10L, "BTC")));
        given(codeGetter.get(2L)).willReturn(Optional.of(mc(20L, "BTC")));
        given(tickGetter.get(1L)).willReturn(Optional.of(new Tick(1L, bidA, askA, 1_000L)));
        given(tickGetter.get(2L)).willReturn(Optional.of(new Tick(2L, bidB, askB, 1_000L)));
        given(exchangeGetter.get(20L)).willReturn(Optional.of(ex("Binance", "USDT")));
        given(exchangeGetter.get(10L)).willReturn(Optional.of(ex("Upbit", "KRW")));
        given(fxGetter.get(new FxKey("USD", "KRW")))
                .willReturn(Optional.of(fx("USD", "KRW", "0.000725")));

        sut.cal(1L);

        // when
        sut.cal(2L);

        // then — 공식: bid = (b.bid*fxB)/(a.ask*fxA) - 1) * 100
        // a = baseTick(idB=2L): bid=36000, ask=36100, fxA=1
        // b = compareTick(idA=1L): bid=50000000, ask=50100000, fxB=0.000725
        BigDecimal fxA = BigDecimal.ONE;
        BigDecimal expectedBid = bidA.multiply(fxVal)
                .divide(askB.multiply(fxA), 8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));
        BigDecimal expectedAsk = askA.multiply(fxVal)
                .divide(bidB.multiply(fxA), 8, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100));

        then(buffer).should(times(1)).add(premiumCaptor.capture());
        Premium captured = premiumCaptor.getValue();
        assertThat(captured.bid()).isEqualByComparingTo(expectedBid);
        assertThat(captured.ask()).isEqualByComparingTo(expectedAsk);
    }

    @Test
    @DisplayName("marketCodeList — base별 id 누적: 등록 후 타 id 페어링 시도 확인")
    void cal_accumulatesMarketCodeListByBase() {
        // given
        setupTwoIds(1L, 2L, "BTC");
        given(exchangeGetter.get(anyLong())).willReturn(Optional.empty()); // handling 조기 중단

        // when
        sut.cal(1L); // marketCodeList["BTC"] = {1L}
        sut.cal(2L); // marketCodeList["BTC"] = {1L, 2L} → 1L 페어링 시도

        // then — cal(2L) 때 1L tick 조회 발생 (페어링 시도 증거)
        then(tickGetter).should(atLeastOnce()).get(1L);
    }

    // ─── 동시성 ──────────────────────────────────────────────────

    @Test
    @Tag("concurrency")
    @DisplayName("동시성 — N 스레드 동시 cal() 호출 시 marketCodeList 손실 없음")
    void cal_concurrency_noLossInMarketCodeList() throws InterruptedException {
        // given
        int n = 10;
        long[] ids = new long[n];
        for (int i = 0; i < n; i++) {
            ids[i] = (long) (i + 1);
            final long id = ids[i];
            final long exId = (long) (i + 10);
            given(codeGetter.get(id)).willReturn(Optional.of(mc(exId, "ETH")));
            given(tickGetter.get(id)).willReturn(Optional.of(tick(id, "2000", "2001")));
            given(exchangeGetter.get(exId)).willReturn(Optional.empty()); // handling 조기 중단
        }

        CountDownLatch ready = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(n);
        ExecutorService pool = Executors.newFixedThreadPool(n);

        for (long id : ids) {
            pool.submit(() -> {
                try {
                    ready.await();
                    sut.cal(id);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        // when
        ready.countDown();
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();

        // then — 모든 id가 marketCodeList에 누적됐으면 이후 임의 id cal() 시 전부 페어링 시도
        // 추가 cal(ids[0]) 으로 나머지 모든 id의 tick 조회가 발생했는지 간접 검증
        sut.cal(ids[0]);
        for (int i = 1; i < n; i++) {
            then(tickGetter).should(atLeastOnce()).get(ids[i]);
        }
    }

    // ─── 헬퍼 ─────────────────────────────────────────────────────

    /**
     * idA(exchangeId=10L), idB(exchangeId=20L) 두 코드를 동일 base로 등록.
     * 공통 코드 → marketCodeList 누적에 사용.
     */
    private void setupTwoIds(long idA, long idB, String base) {
        given(codeGetter.get(idA)).willReturn(Optional.of(mc(10L, base)));
        given(codeGetter.get(idB)).willReturn(Optional.of(mc(20L, base)));
        given(tickGetter.get(idA)).willReturn(Optional.of(tick(idA, "50000000", "50100000")));
        given(tickGetter.get(idB)).willReturn(Optional.of(tick(idB, "36000", "36100")));
    }
}
