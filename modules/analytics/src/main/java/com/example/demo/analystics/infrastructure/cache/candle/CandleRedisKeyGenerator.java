package com.example.demo.analystics.infrastructure.cache.candle;

import com.example.demo.infra_shard.redis.RedisKeys;
import org.springframework.stereotype.Component;

@Component
public class CandleRedisKeyGenerator {
    public  String tickCandleState(String env , int partitionId, String tf) {
        return RedisKeys.tickCandleState(env, partitionId, tf);
    }

    public  String premiumCandleState(String env ,int partitionId, String tf) {
        return RedisKeys.premiumCandleState(env, partitionId, tf);
    }

    public String premiumDetailCandleState(String env,int partitionId, String tf) {
        return RedisKeys.premiumDetailCandleState(env, partitionId, tf);
    }

}
