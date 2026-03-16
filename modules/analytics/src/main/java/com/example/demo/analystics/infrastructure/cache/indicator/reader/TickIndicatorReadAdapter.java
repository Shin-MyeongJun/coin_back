package com.example.demo.analystics.infrastructure.cache.indicator.reader;

import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.infrastructure.cache.indicator.IndicatorRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.IndicatorStateCodecManager;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.IndicatorKeyCodec;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TickIndicatorReadAdapter extends IndicatorStateReadAdapter<TickKey> {
    public TickIndicatorReadAdapter(RedisTemplate<String, String> redis, IndicatorKeyCodec<TickKey> keyCodec, IndicatorStateCodecManager stateCodecManager, IndicatorRedisKeyGenerator keyGenerator) {
        super(redis, keyCodec, stateCodecManager, keyGenerator);
    }

    @Override
    protected String makeKey(String env,int partitionId, String tf) {
        return keyGenerator.tickIndicatorState(env,partitionId,tf);
    }
}
