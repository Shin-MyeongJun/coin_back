package com.example.demo.analystics.infrastructure.cache.candle.reader;

import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.infrastructure.cache.candle.CandleRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.candle.codec.CandleStateCodec;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.DataKeyCodec;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PremiumCandleStateReadAdapter
        extends CandleStateReadAdapter<PremiumCandle,PremiumKey,BigDecimal>{

    public PremiumCandleStateReadAdapter(RedisTemplate<String, String> redis, CandleStateCodec<PremiumCandle, BigDecimal> stateCodec, DataKeyCodec<PremiumKey> keyCodec, CandleRedisKeyGenerator redisKeyGenerator) {
        super(redis, stateCodec, keyCodec, redisKeyGenerator);
    }

    @Override
    protected String makeKey(String env,int partitionId ,String tf) {
        return keyGenerator.premiumCandleState(env,partitionId ,tf);
    }
}
