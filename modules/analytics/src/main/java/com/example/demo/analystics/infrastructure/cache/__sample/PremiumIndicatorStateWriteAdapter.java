package com.example.demo.analystics.infrastructure.cache.__sample;

import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.indicator.open.PremiumIndicator;
import com.example.demo.infra_shard.redis.RedisKeys;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PremiumIndicatorStateWriteAdapter implements WriteAnalyticsStatePort<PremiumIndicator> {

    private final RedisTemplate<String, String> redis;

    private final String env = "local";
    private static final Duration LATEST_TTL = Duration.ofSeconds(30);
    private static final StringRedisSerializer S = new StringRedisSerializer();


    private static final byte[] F_OPTS       = S.serialize("openTs");
    private static final byte[] F_VAL = S.serialize("value");
    private static final byte[] F_PAYLOAD = S.serialize("payload");



    @Override
    public void upsertAll(Interval ind, List<PremiumIndicator> pis) {
        if (pis == null || pis.isEmpty()) return;
        String tf = ind.getPeriod();
        redis.executePipelined((RedisConnection connection) -> {
            for (PremiumIndicator pi : pis) {
                if (pi == null) continue;
                byte[] rawKey = S.serialize(RedisKeys.premiumIndicatorState
                        (env, pi.getBaseExchangeId(), pi.getCompareExchangeId(), pi.getBase(),
                                tf,pi.getIndicatorType().name(),Integer.toString(pi.getPeriod()) ));
                byte[] vOpTs  = S.serialize(Long.toString(pi.getOpenTimestamp()));
                byte[] vVal = S.serialize(pi.getValue().toPlainString());
                byte[] vPayload =  S.serialize(pi.getPayload().);
                final Map<byte[], byte[]> fields = new HashMap<>(6);
                fields.put(F_OPTS, vOpTs);
                fields.put(F_VAL, vVal);
                fields.put(F_PAYLOAD, vPayload);

                connection.hashCommands().hMSet(rawKey, fields);
                connection.keyCommands().expire(rawKey, LATEST_TTL.toSeconds());
            }
            return null;
        });
    }
}
