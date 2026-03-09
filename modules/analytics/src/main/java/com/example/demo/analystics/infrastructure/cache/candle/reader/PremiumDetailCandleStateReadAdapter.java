package com.example.demo.analystics.infrastructure.cache.candle.reader;

import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
import com.example.demo.analystics.domain.domain.candle.value.PremiumDetailValue;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.infrastructure.cache.candle.CandleRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.candle.codec.CandleStateCodec;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.DataKeyCodec;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PremiumDetailCandleStateReadAdapter
        extends CandleStateReadAdapter<PremiumDetailCandle,PremiumKey,PremiumDetailValue> {
    public PremiumDetailCandleStateReadAdapter(RedisTemplate<String, String> redis, CandleStateCodec<PremiumDetailCandle, PremiumDetailValue> stateCodec, DataKeyCodec<PremiumKey> keyCodec, CandleRedisKeyGenerator keyGenerator) {
        super(redis, stateCodec, keyCodec, keyGenerator);
    }

    @Override
    protected String makeKey(String env,int partitionId ,String tf) {
        return keyGenerator.premiumDetailCandleState(env,partitionId ,tf);
    }
}

