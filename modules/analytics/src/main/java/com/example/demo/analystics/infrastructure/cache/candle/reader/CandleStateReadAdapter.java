package com.example.demo.analystics.infrastructure.cache.candle.reader;


import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.candle.open.OpenCandle;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryCandleState;
import com.example.demo.analystics.infrastructure.cache.candle.CandleRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.candle.codec.CandleStateCodec;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.DataKeyCodec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public abstract   class CandleStateReadAdapter<CANDLE extends OpenCandle<KEY,VAL>,KEY extends DataKey<KEY>,VAL extends Comparable<VAL>>
        implements ReadAnalyticsStatePort< KEY,RecoveryCandleState<VAL>> {

    private final RedisTemplate<String, String> redis;
    private final CandleStateCodec<CANDLE, VAL> stateCodec;
    private final DataKeyCodec<KEY> keyCodec;
    protected final CandleRedisKeyGenerator keyGenerator;
    @Value("${app.env:local}") private String env;

    private final ScanOptions options = ScanOptions.scanOptions()
            .match("*")
            .count(1000)
            .build();

    public Map<KEY, RecoveryCandleState<VAL>> read(int partitionId,Interval interval) {
        String redisKey = makeKey(env,partitionId,interval.getPeriod());
        Map<KEY, RecoveryCandleState<VAL>> result = new HashMap<>();

        try (Cursor<Map.Entry<Object, Object>> cursor =
                     redis.opsForHash().scan(redisKey, options)) {
            while (cursor.hasNext()) {
                Map.Entry<Object, Object> entry = cursor.next();
                byte[] field = (byte[]) entry.getKey();
                byte[] value = (byte[]) entry.getValue();
                result.put(keyCodec.decode(field), stateCodec.decode(value));
            }
        } catch (Exception e) {
            log.warn("Redis state scan failed: redisKey={}", redisKey, e);
        }
        return result;
    }

    protected abstract String makeKey(String env,int partitionId, String tf);
}
