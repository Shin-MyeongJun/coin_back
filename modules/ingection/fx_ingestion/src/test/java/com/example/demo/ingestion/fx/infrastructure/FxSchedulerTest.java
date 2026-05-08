package com.example.demo.ingestion.fx.infrastructure;

import com.example.demo.ingestion.fx.application.port.in.IngestAndPublishFxUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class FxSchedulerTest {

    @Mock
    IngestAndPublishFxUseCase useCase;

    @InjectMocks
    FxScheduler sut;

    @Test
    @DisplayName("process — useCase.run('USD','KRW') 호출")
    void process_triggersUseCase() {
        sut.process();

        then(useCase).should().run("USD", "KRW");
    }
}
