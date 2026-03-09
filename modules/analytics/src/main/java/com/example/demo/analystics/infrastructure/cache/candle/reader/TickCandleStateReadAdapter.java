package com.example.demo.analystics.infrastructure.cache.candle.reader;

import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.infrastructure.cache.candle.CandleRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.candle.codec.CandleStateCodec;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.DataKeyCodec;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TickCandleStateReadAdapter
        extends CandleStateReadAdapter<TickCandle,TickKey,BigDecimal> {

    public TickCandleStateReadAdapter(RedisTemplate<String, String> redis, CandleStateCodec<TickCandle, BigDecimal> stateCodec, DataKeyCodec<TickKey> keyCodec, CandleRedisKeyGenerator keyGenerator) {
        super(redis, stateCodec, keyCodec, keyGenerator);
    }

    @Override
    protected String makeKey(String env,int partitionId ,String tf) {
        return keyGenerator.tickCandleState(env,partitionId ,tf);
    }
}
