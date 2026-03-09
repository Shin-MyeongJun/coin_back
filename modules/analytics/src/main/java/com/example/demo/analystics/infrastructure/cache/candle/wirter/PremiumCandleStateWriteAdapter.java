package com.example.demo.analystics.infrastructure.cache.candle.wirter;

import com.example.demo.analystics.domain.domain.candle.open.PremiumCandle;
import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.infrastructure.cache.CacheExpirationPolicy;
import com.example.demo.analystics.infrastructure.cache.candle.CandleRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.candle.codec.CandleStateCodec;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.DataKeyCodec;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class PremiumCandleStateWriteAdapter extends CandleStateWriteAdapter<
        PremiumCandle, PremiumKey, BigDecimal> {
    public PremiumCandleStateWriteAdapter(RedisTemplate<String, String> redis, CandleStateCodec<PremiumCandle, BigDecimal> stateCodec, DataKeyCodec<PremiumKey> keyCodec, CacheExpirationPolicy policy, CandleRedisKeyGenerator keyGenerator) {
        super(redis, stateCodec, keyCodec, policy, keyGenerator);
    }
    @Override
    protected String makeKey(String env,int partitionId ,String tf) {
        return keyGenerator.premiumCandleState(env,partitionId ,tf);
    }
}