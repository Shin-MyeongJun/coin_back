package com.example.demo.analystics.infrastructure.cache.indicator.writer;

import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.infrastructure.cache.CacheExpirationPolicy;
import com.example.demo.analystics.infrastructure.cache.indicator.IndicatorRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.IndicatorStateCodecManager;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.IndicatorKeyCodec;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PremiumIndicatorStateWriteAdapter extends IndicatorStateWriteAdapter<PremiumKey, PremiumIndicator> {


    public PremiumIndicatorStateWriteAdapter(RedisTemplate<String, String> redis, IndicatorStateCodecManager manager, IndicatorKeyCodec<PremiumKey> keyCodec, CacheExpirationPolicy policy, IndicatorRedisKeyGenerator keyGenerator) {
        super(redis, manager, keyCodec, policy, keyGenerator);
    }

    @Override
    protected String makeKey(String env,int partitionId , String tf) {
        return keyGenerator.premiumIndicatorState(env,partitionId ,tf);
    }
}
