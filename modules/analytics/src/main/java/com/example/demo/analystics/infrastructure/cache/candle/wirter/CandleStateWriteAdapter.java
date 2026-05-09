package com.example.demo.analystics.infrastructure.cache.candle.wirter;

import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.open.OpenCandle;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.infrastructure.cache.CacheExpirationPolicy;
import com.example.demo.analystics.infrastructure.cache.candle.CandleRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.candle.codec.CandleStateCodec;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.DataKeyCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@RequiredArgsConstructor
public abstract class CandleStateWriteAdapter<
         CANDLE extends OpenCandle<KEY,VAL>,
         KEY extends DataKey<KEY>,
         VAL extends Comparable<VAL>
>
        implements WriteAnalyticsStatePort<CANDLE> {

    private final RedisTemplate<String, String> redis;
    private final CandleStateCodec<CANDLE,  VAL> stateCodec;
    private final DataKeyCodec<KEY> keyCodec;

    private final CacheExpirationPolicy policy;
    protected final CandleRedisKeyGenerator keyGenerator;

    @Value("${app.env:local}") private String env;


    public void upsert(int partitionId ,Interval interval, List<CANDLE> cs) {
        if (cs == null || cs.isEmpty()) return;

        final String tf = interval.getPeriod();
        final long tfSecond = interval.getSecond();
        final long openTime =cs.getFirst().getOpenTimestamp();
        byte[] rawKey = makeKey(env,partitionId,tf).getBytes();
        redis.executePipelined((RedisCallback<Object>) connection -> {
            for (CANDLE c : cs) {
                if (c == null) continue;
                byte[] field =  keyCodec.encode(c.getKey());
                byte[] payload = stateCodec.encode(c);
                connection.hashCommands().hSet(rawKey, field, payload);
            }
            connection.keyCommands().expire(rawKey, policy.calculateTtl(openTime,tfSecond));
            return null;
        });

    }
    protected abstract String makeKey(String env,int partitionId ,String tf);
}
