package com.example.demo.analystics.infrastructure.cache.indicator;

import com.example.demo.infra_shard.redis.RedisKeys;
import org.springframework.stereotype.Component;

@Component
public class IndicatorRedisKeyGenerator {

    // Indicator state (tf + type required)
    public  String tickIndicatorState(String env,int partitionId,String tf) {
        return RedisKeys.tickIndicatorState(env, partitionId, tf);
    }

    public  String premiumIndicatorState(String env, int partitionId, String tf) {
        return RedisKeys.premiumIndicatorState(env, partitionId, tf);
    }
}
