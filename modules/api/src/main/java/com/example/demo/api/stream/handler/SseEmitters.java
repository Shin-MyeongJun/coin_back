package com.example.demo.api.stream.handler;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

final class SseEmitters {

    private SseEmitters() {
    }

    static boolean sendConnected(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("{}"));
            return true;
        } catch (Exception e) {
            emitter.completeWithError(e);
            return false;
        }
    }
}
