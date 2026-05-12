package com.example.demo.api.stream.handler;

import com.example.demo.api.stream.sink.MarketDataStream;
import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumDetailSseHandler {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final MarketDataStream marketDataStream;
    private final ObjectMapper objectMapper;

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        Disposable subscription = marketDataStream.premiumDetailSink.asFlux()
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

    private void sendEvent(SseEmitter emitter, PremiumDetailMessage msg) {
        try {
            emitter.send(SseEmitter.event()
                    .name("premium-detail")
                    .data(objectMapper.writeValueAsString(msg)));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
