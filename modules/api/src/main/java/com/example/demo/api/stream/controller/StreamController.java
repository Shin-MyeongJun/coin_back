package com.example.demo.api.stream.controller;

import com.example.demo.api.stream.handler.CandleCloseSseHandler;
import com.example.demo.api.stream.handler.IndicatorCloseSseHandler;
import com.example.demo.api.stream.handler.PremiumDetailSseHandler;
import com.example.demo.api.stream.handler.PremiumSseHandler;
import com.example.demo.api.stream.handler.TickSseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
public class StreamController {

    private final TickSseHandler tickSseHandler;
    private final PremiumSseHandler premiumSseHandler;
    private final PremiumDetailSseHandler premiumDetailSseHandler;
    private final CandleCloseSseHandler candleCloseSseHandler;
    private final IndicatorCloseSseHandler indicatorCloseSseHandler;

    @GetMapping(value = "/ticks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTicks(
            @RequestParam(required = false) Long marketCodeId) {
        return tickSseHandler.subscribe(marketCodeId);
    }

    @GetMapping(value = "/premium", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPremium() {
        return premiumSseHandler.subscribe();
    }

    @GetMapping(value = "/premium-detail/raw", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPremiumDetailRaw() {
        return premiumDetailSseHandler.subscribe();
    }

    @GetMapping(value = "/candles/close", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCandleClose(
            @RequestParam(defaultValue = "tick") String type) {
        return candleCloseSseHandler.subscribe(type);
    }

    @GetMapping(value = "/indicators/close", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamIndicatorClose(
            @RequestParam(defaultValue = "tick") String type) {
        return indicatorCloseSseHandler.subscribe(type);
    }
}
