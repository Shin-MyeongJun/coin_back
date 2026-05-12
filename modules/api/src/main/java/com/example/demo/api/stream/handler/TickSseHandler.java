package com.example.demo.api.stream.handler;

import com.example.demo.api.stream.sink.MarketDataStream;
import com.example.demo.contracts.message.price_value.TickMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

@Slf4j
@Component
@RequiredArgsConstructor
public class TickSseHandler {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final MarketDataStream marketDataStream;
    private final ObjectMapper objectMapper;

    public SseEmitter subscribe(Long marketCodeId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        Disposable subscription = marketDataStream.tickSink.asFlux()
                .filter(msg -> marketCodeId == null || marketCodeId.equals(msg.marketCodeId()))
                .subscribe(
                        msg -> sendEvent(emitter, msg),
                        err -> emitter.completeWithError(err),
                        emitter::complete
                );
        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(subscription::dispose);
        emitter.onError(e -> subscription.dispose());
        if (!SseEmitters.sendConnected(emitter)) {
            subscription.dispose();
        }
        return emitter;
    }

    private void sendEvent(SseEmitter emitter, TickMessage msg) {
        try {
            emitter.send(SseEmitter.event()
                    .name("tick")
                    .data(objectMapper.writeValueAsString(msg)));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
