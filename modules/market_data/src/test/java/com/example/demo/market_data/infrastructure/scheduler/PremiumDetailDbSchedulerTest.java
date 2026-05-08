package com.example.demo.market_data.infrastructure.scheduler;

import com.example.demo.market_data.application.port.in.FlushPriceValueBufferUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PremiumDetailDbSchedulerTest {

    @Mock FlushPriceValueBufferUseCase.ForPremiumDetail useCase;

    @Test
    @DisplayName("process() 호출 시 ForPremiumDetail.flush() 1회 위임")
    void process_delegatesToUseCaseFlush() {
        // given
        PremiumDetailDbScheduler sut = new PremiumDetailDbScheduler(useCase);

        // when
        sut.process();

        // then
        then(useCase).should().flush();
    }
}
