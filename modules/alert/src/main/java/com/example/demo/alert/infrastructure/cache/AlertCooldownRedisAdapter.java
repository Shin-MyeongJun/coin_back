package com.example.demo.alert.infrastructure.cache;

import com.example.demo.alert.application.port.out.CooldownGuardPort;
import com.example.demo.alert.domain.service.CooldownDecision;
import com.example.demo.infra_shard.redis.RedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AlertCooldownRedisAdapter implements CooldownGuardPort {
    private final StringRedisTemplate redisTemplate;
    private final Environment environment;

    @Override
    public CooldownDecision tryAcquire(long ruleId, int cooldownSec) {
        String appEnv = environment.getProperty("app.env", "dev");
        String key = RedisKeys.alertCooldown(appEnv, ruleId);
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(cooldownSec));
        if (Boolean.TRUE.equals(acquired)) {
            return CooldownDecision.acquired(key, cooldownSec);
        }
        return CooldownDecision.rejected(key, cooldownSec);
    }
}
