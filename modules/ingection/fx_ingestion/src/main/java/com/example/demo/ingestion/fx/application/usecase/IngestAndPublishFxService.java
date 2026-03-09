package com.example.demo.ingestion.fx.application.usecase;

import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.ingestion.fx.application.port.in.IngestAndPublishFxUseCase;
import com.example.demo.ingestion.fx.application.port.in.IngestFxDataUseCase;
import com.example.demo.ingestion.fx.application.port.out.PublishFxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngestAndPublishFxService implements IngestAndPublishFxUseCase {
    private final PublishFxPort publishPort;
    private final IngestFxDataUseCase useCase;

    @Override
    public void run(String quote, String base) {
        //일단 달러만 처리
        FxMessage message = useCase.get("USD","KRW");
        publishPort.publish(message);
    }
}
