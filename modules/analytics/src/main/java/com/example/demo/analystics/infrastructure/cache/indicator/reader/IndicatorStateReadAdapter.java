package com.example.demo.analystics.infrastructure.cache.indicator.reader;

import com.example.demo.analystics.application.port.out.ReadAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.key.DataKey;
import com.example.demo.analystics.domain.domain.key.IndicatorPriceDataKey;
import com.example.demo.analystics.domain.domain.recovery.RecoveryIndicatorState;
import com.example.demo.analystics.infrastructure.cache.indicator.IndicatorRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.IndicatorStateCodecManager;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.IndicatorKeyCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public abstract class IndicatorStateReadAdapter<KEY extends DataKey<KEY>> implements ReadAnalyticsStatePort<IndicatorPriceDataKey<KEY>,RecoveryIndicatorState> {

    private final RedisTemplate<String, String> redis;
    private final IndicatorKeyCodec<KEY> keyCodec;
    private final IndicatorStateCodecManager stateCodecManager;
    protected final IndicatorRedisKeyGenerator keyGenerator;
    private final String env ="local";

    private final ScanOptions options = ScanOptions.scanOptions()
            .match("*")
            .count(1000)
            .build();
    @Override
    public Map<IndicatorPriceDataKey<KEY>, RecoveryIndicatorState> read(int partitionId, Interval interval) {
        String redisKey = makeKey(env,partitionId,interval.getPeriod());
        Map<IndicatorPriceDataKey<KEY>, RecoveryIndicatorState> result = new HashMap<>();

        try (Cursor<Map.Entry<Object, Object>> cursor =
                     redis.opsForHash().scan(redisKey, options)) {
            while (cursor.hasNext()) {
                Map.Entry<Object, Object> entry = cursor.next();
                byte[] field = (byte[]) entry.getKey();
                byte[] value = (byte[]) entry.getValue();
                IndicatorPriceDataKey<KEY> key = keyCodec.decode(field);
                result.put(key, stateCodecManager.decode(key.indicatorKey().type(),value));
            }
        } catch (Exception e) {

        }
        return result;
    }

    protected abstract String makeKey(String env,int partitionId ,String tf);
}
