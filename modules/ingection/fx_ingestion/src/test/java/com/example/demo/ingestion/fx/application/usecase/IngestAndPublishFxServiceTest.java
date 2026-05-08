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
    @DisplayName("run — useCase.get('USD','KRW') 호출 후 publishPort.publish에 전달")
    void run_getsAndPublishes() {
        // given
        FxMessage msg = new FxMessage("USD/KRW", "USD", "KRW", new BigDecimal("1320.50"), 1_000L);
        given(useCase.get("USD", "KRW")).willReturn(msg);

        // when
        sut.run("USD", "KRW");

        // then
        then(useCase).should().get("USD", "KRW");
        then(publishPort).should().publish(msg);
    }

    @Test
    @DisplayName("run — quote/base 인수 무관하게 항상 'USD','KRW'로 조회")
    void run_alwaysUsesUsdKrw() {
        // given
        FxMessage msg = new FxMessage("USD/KRW", "USD", "KRW", new BigDecimal("1300"), 2_000L);
        given(useCase.get("USD", "KRW")).willReturn(msg);

        // when  — 인수가 달라도 내부에서 USD/KRW 고정
        sut.run("EUR", "JPY");

        // then
        then(useCase).should().get("USD", "KRW");
    }
}
