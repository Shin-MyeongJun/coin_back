package com.example.demo.api.config.security.ssetoken;

import com.example.demo.infra_shard.redis.RedisKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RedisSseTicketStoreAdapter implements ConsumeSseTicketPort {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.env:local}")
    private String env;

    @Override
    public boolean store(String token, SseTicketPayload payload, Duration ttl) {
        String key = RedisKeys.sseTicket(env, token);
        String json = serialize(payload);
        Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, json, ttl);
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public Optional<SseTicketPayload> consume(String token) {
        if (token == null || token.isBlank()) return Optional.empty();
        String key = RedisKeys.sseTicket(env, token);
        String json = redisTemplate.opsForValue().getAndDelete(key);
        if (json == null) return Optional.empty();
        return Optional.of(deserialize(json));
    }

    private String serialize(SseTicketPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize SseTicketPayload", e);
        }
    }

    private SseTicketPayload deserialize(String json) {
        try {
            return objectMapper.readValue(json, SseTicketPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize SseTicketPayload", e);
        }
    }
}
