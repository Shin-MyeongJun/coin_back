package com.example.demo.ingestion.exchange.infrastruct.scheduler;

import com.example.demo.ingestion.exchange.application.port.in.HandleMetaRawUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MarketCodeSchedulerTest {

    @Mock
    HandleMetaRawUseCase handler;

    @InjectMocks
    MarketCodeScheduler sut;

    @Test
    @DisplayName("process — handler.process() 1회 호출")
    void process_callsHandlerProcess() {
        // when
        sut.process();

        // then
        then(handler).should().process();
    }

    @Test
    @DisplayName("onApplicationReady — handler.process() 1회 호출")
    void onApplicationReady_callsHandlerProcess() {
        // when
        sut.onApplicationReady();

        // then
        then(handler).should().process();
    }
}
