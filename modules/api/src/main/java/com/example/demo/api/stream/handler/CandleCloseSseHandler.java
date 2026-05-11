package com.example.demo.api.stream.handler;

import com.example.demo.api.stream.sink.AnalyticsStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

@Slf4j
@Component
@RequiredArgsConstructor
public class CandleCloseSseHandler {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final AnalyticsStream analyticsStream;
    private final ObjectMapper objectMapper;

    public SseEmitter subscribe(String type) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        Disposable subscription = switch (type) {
            case "premium" -> analyticsStream.premiumCandleSink.asFlux()
                    .subscribe(
                            msg -> sendEvent(emitter, "candle-close", msg),
                            err -> emitter.completeWithError(err),
                            emitter::complete
                    );
            case "premium-detail" -> analyticsStream.premiumDetailCandleSink.asFlux()
                    .subscribe(
                            msg -> sendEvent(emitter, "candle-close", msg),
                            err -> emitter.completeWithError(err),
                            emitter::complete
                    );
            default -> analyticsStream.tickCandleSink.asFlux()
                    .subscribe(
                            msg -> sendEvent(emitter, "candle-close", msg),
                            err -> emitter.completeWithError(err),
                            emitter::complete
                    );
        };
        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(subscription::dispose);
        emitter.onError(e -> subscription.dispose());
        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object msg) {
        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(objectMapper.writeValueAsString(msg)));
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }
}
