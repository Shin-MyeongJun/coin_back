package com.example.demo.analystics.infrastructure.cache.indicator.writer;

import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.open.OpenTradeIndicator;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.infrastructure.cache.CacheExpirationPolicy;
import com.example.demo.analystics.infrastructure.cache.indicator.IndicatorRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.IndicatorStateCodecManager;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.IndicatorKeyCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@RequiredArgsConstructor
public abstract class IndicatorStateWriteAdapter<
         KEY extends DataKey<KEY>,
         IND extends OpenTradeIndicator<KEY>
>
        implements WriteAnalyticsStatePort<IND> {

    private final RedisTemplate<String, String> redis;
    private final IndicatorStateCodecManager manager;
    private final IndicatorKeyCodec<KEY> keyCodec;

    private final CacheExpirationPolicy policy;
    protected final IndicatorRedisKeyGenerator keyGenerator;

    @Value("${app.env:local}") private String env;


    @Override
    public void upsert(int partitionId ,Interval interval, List<IND> cs) {
        if (cs == null || cs.isEmpty()) return;

        final String tf = interval.getPeriod();
        final long tfSecond = interval.getSecond();
        final long openTime =cs.get(0).getOpenTimestamp();
        byte[] rawKey = makeKey(env,partitionId,tf).getBytes();
        redis.executePipelined((RedisCallback<Object>) connection -> {
            for (IND ind : cs) {
                if (ind == null) continue;
                byte[] field =  keyCodec.encode(ind.getDataKey(), ind.getIndicatorKey());
                byte[] payload = manager.encode(ind.getIndicatorKey().type(),ind.getPayload(), ind.getOpenTimestamp());
                connection.hashCommands().hSet(rawKey, field, payload);
            }
            connection.keyCommands().expire(rawKey, policy.calculateTtl(openTime,tfSecond));
            return null;
        });

    }
    protected abstract String makeKey(String env,int partitionId ,String tf);
}
