package com.example.demo.ingestion.fx.infrastructure;

import com.example.demo.ingestion.fx.application.port.in.IngestAndPublishFxUseCase;
import com.example.demo.ingestion.fx.application.port.out.FxMessageSchdeuled;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FxScheduler implements FxMessageSchdeuled {

    private final IngestAndPublishFxUseCase useCase;
    @Override
    @Scheduled(fixedRate = 1000)
    public void process() {
        useCase.run("USD","KRW");
    }
}
