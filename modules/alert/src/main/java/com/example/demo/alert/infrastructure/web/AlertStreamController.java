package com.example.demo.alert.infrastructure.web;

import com.example.demo.alert.infrastructure.sse.AlertSseRegistry;
import com.example.demo.user.application.port.in.AuthenticatedAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/stream/alerts")
@RequiredArgsConstructor
public class AlertStreamController {
    private final AlertSseRegistry registry;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal AuthenticatedAccount account) {
        return registry.register(account.id().asString());
    }
}
