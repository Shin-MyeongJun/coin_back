package com.example.demo.alert.infrastructure.web;

import com.example.demo.alert.infrastructure.sse.AlertSseRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/stream/alerts")
@RequiredArgsConstructor
public class AlertStreamController {
    private final AlertSseRegistry registry;

    @GetMapping
    public SseEmitter stream(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return registry.register(resolveUserId(userId));
    }

    private String resolveUserId(String userId) {
        return userId == null || userId.isBlank() ? "anonymous" : userId;
    }
}
