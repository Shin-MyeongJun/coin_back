package com.example.demo.analystics.infrastructure.cache.indicator.writer;

import com.example.demo.analystics.domain.domain.indicator.open.TickIndicator;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.infrastructure.cache.CacheExpirationPolicy;
import com.example.demo.analystics.infrastructure.cache.indicator.IndicatorRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.IndicatorStateCodecManager;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.IndicatorKeyCodec;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorStateWriteAdapter extends IndicatorStateWriteAdapter<TickKey, TickIndicator> {
    public TickIndicatorStateWriteAdapter(RedisTemplate<String, String> redis, IndicatorStateCodecManager manager, IndicatorKeyCodec<TickKey> keyCodec, CacheExpirationPolicy policy, IndicatorRedisKeyGenerator keyGenerator) {
        super(redis, manager, keyCodec, policy, keyGenerator);
    }

    @Override
    protected String makeKey(String env,int partitionId , String tf) {
        return keyGenerator.tickIndicatorState(env,partitionId,tf);
    }
}
