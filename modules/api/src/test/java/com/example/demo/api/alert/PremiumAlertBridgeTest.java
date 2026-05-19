package com.example.demo.api.alert;

import com.example.demo.alert.application.port.in.EvaluatePremiumAlertUseCase;
import com.example.demo.api.stream.sink.MarketDataStream;
import com.example.demo.contracts.message.price_value.PremiumMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PremiumAlertBridgeTest {

    private MarketDataStream marketDataStream;
    private EvaluatePremiumAlertUseCase useCase;
    private ObjectProvider<EvaluatePremiumAlertUseCase> useCaseProvider;
    private PremiumAlertBridge sut;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        marketDataStream = spy(new MarketDataStream());
        useCase = mock(EvaluatePremiumAlertUseCase.class);
        useCaseProvider = mock(ObjectProvider.class);
        when(useCaseProvider.getIfAvailable()).thenReturn(useCase);
        sut = new PremiumAlertBridge(marketDataStream, useCaseProvider);
    }

    @Test
    @DisplayName("enabled=false면 premiumSink 구독을 만들지 않음")
    void disabled_doesNotSubscribe() {
        ReflectionTestUtils.setField(sut, "enabled", false);

        sut.start();
        marketDataStream.premiumSink.tryEmitNext(premium("BTC"));

        verify(useCase, never()).evaluate(any());
    }

    @Test
    @DisplayName("enabled=true면 premiumSink emit이 useCase.evaluate로 전달")
    void enabled_subscribesAndDispatches() {
        ReflectionTestUtils.setField(sut, "enabled", true);

        sut.start();
        PremiumMessage message = premium("BTC");
        marketDataStream.premiumSink.tryEmitNext(message);

        verify(useCase, times(1)).evaluate(message);
    }

    @Test
    @DisplayName("useCase가 IllegalArgumentException을 던져도 구독은 살아남음")
    void useCaseThrowsIllegalArgument_subscriptionSurvives() {
        ReflectionTestUtils.setField(sut, "enabled", true);
        AtomicReference<PremiumMessage> firstSeen = new AtomicReference<>();
        org.mockito.Mockito.doAnswer(inv -> {
            firstSeen.set(inv.getArgument(0));
            throw new IllegalArgumentException("CROSSES_ABOVE not supported");
        }).when(useCase).evaluate(any());

        sut.start();
        marketDataStream.premiumSink.tryEmitNext(premium("BTC"));

        assertThat(firstSeen.get()).isNotNull();
        org.mockito.Mockito.reset(useCase);
        marketDataStream.premiumSink.tryEmitNext(premium("ETH"));
        verify(useCase, times(1)).evaluate(any());
    }

    @Test
    @DisplayName("@PreDestroy stop은 구독을 dispose")
    void stop_disposesSubscription() {
        ReflectionTestUtils.setField(sut, "enabled", true);
        sut.start();

        sut.stop();
        marketDataStream.premiumSink.tryEmitNext(premium("BTC"));

        verify(useCase, never()).evaluate(any());
    }

    @Test
    @DisplayName("enabled=true이지만 useCase bean 없으면 구독 미발생")
    void enabled_but_useCaseMissing_doesNotSubscribe() {
        ReflectionTestUtils.setField(sut, "enabled", true);
        when(useCaseProvider.getIfAvailable()).thenReturn(null);

        sut.start();
        marketDataStream.premiumSink.tryEmitNext(premium("BTC"));

        verify(useCase, never()).evaluate(any());
    }

    private static PremiumMessage premium(String symbol) {
        return new PremiumMessage(symbol, 1L, 2L, new BigDecimal("1.0"), new BigDecimal("1.1"), 1_000L);
    }
}
