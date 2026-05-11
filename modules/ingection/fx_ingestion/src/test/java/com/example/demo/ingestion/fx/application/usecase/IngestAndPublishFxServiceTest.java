package com.example.demo.ingestion.fx.application.usecase;

import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.ingestion.fx.application.port.in.IngestFxDataUseCase;
import com.example.demo.ingestion.fx.application.port.out.PublishFxPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class IngestAndPublishFxServiceTest {

    @Mock
    PublishFxPort publishPort;

    @Mock
    IngestFxDataUseCase useCase;

    @InjectMocks
    IngestAndPublishFxService sut;

    @Test
    @DisplayName("run은 전달받은 base/quote로 FX를 조회하고 발행한다")
    void runGetsAndPublishesRequestedPair() {
        FxMessage msg = new FxMessage("USD/KRW", "USD", "KRW", new BigDecimal("1320.50"), 1_000L);
        given(useCase.get("USD", "KRW")).willReturn(msg);

        sut.run("USD", "KRW");

        then(useCase).should().get("USD", "KRW");
        then(publishPort).should().publish(msg);
    }

    @Test
    @DisplayName("run은 USD/KRW 외의 요청도 고정값으로 덮지 않는다")
    void runDoesNotForceUsdKrw() {
        FxMessage msg = new FxMessage("EUR/JPY", "EUR", "JPY", new BigDecimal("160"), 2_000L);
        given(useCase.get("EUR", "JPY")).willReturn(msg);

        sut.run("EUR", "JPY");

        then(useCase).should().get("EUR", "JPY");
        then(publishPort).should().publish(msg);
    }

    @Test
    @DisplayName("run은 FX 조회 실패 시 null 메시지를 발행하지 않는다")
    void runDoesNotPublishNullMessage() {
        given(useCase.get("USD", "KRW")).willReturn(null);

        sut.run("USD", "KRW");

        then(publishPort).shouldHaveNoInteractions();
    }
}
