//package com.example.demo.analystics.infrastructure.cache.__sample;
//
//import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
//import com.example.demo.analystics.domain.domain.Interval;
//import com.example.demo.analystics.domain.domain.candle.open.TickCandle;
//import com.example.demo.infra_shard.redis.RedisKeys;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.core.RedisCallback;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//public class TickCandleStateWriteAdapter implements WriteAnalyticsStatePort<TickCandle> {
//    private final RedisTemplate<String, String> redis;
//
//    private final String env = "local";
//    private static final Duration LATEST_TTL = Duration.ofSeconds(30);
//
//    private static final StringRedisSerializer S = new StringRedisSerializer();
//
//    // field name bytes (미리 직렬화해서 비용 절감)
//    private static final byte[] F_OPTS       = S.serialize("openTs");
//    private static final byte[] F_OPEN = S.serialize("open");
//    private static final byte[] F_HIGH = S.serialize("high");
//    private static final byte[] F_LOW = S.serialize("low");
//    private static final byte[] F_CLOSE = S.serialize("close");
//
//    @Override
//    public void upsertAll(Interval interval, List<TickCandle> tcs) {
//        if (tcs == null || tcs.isEmpty()) return;
//        String tf = interval.getPeriod();
//        redis.executePipelined((RedisCallback<Object>) (RedisConnection connection) -> {
//            for (TickCandle tc : tcs) {
//                if (tc == null) continue;
//
//                byte[] rawKey = S.serialize(RedisKeys.tickCandleState(env,tc.getMarketCodeId(),tf));
//                byte[] vOpTs  = S.serialize(Long.toString(tc.getOpenTimestamp()));
//                byte[] vOpen = S.serialize(tc.getOpen().toPlainString());
//                byte[] vHigh =  S.serialize(tc.getHigh().toPlainString());
//                byte[] vLow = S.serialize(tc.getLow().toPlainString());
//                byte[] vClose =  S.serialize(tc.getClose().toPlainString());
//                final Map<byte[], byte[]> fields = new HashMap<>(6);
//                fields.put( F_OPTS , vOpTs);
//                fields.put( F_OPEN, vOpen);
//                fields.put( F_HIGH, vHigh);
//                fields.put( F_LOW, vLow);
//                fields.put( F_CLOSE, vClose);
//                connection.hashCommands().hMSet(rawKey, fields);
//                connection.keyCommands().expire(rawKey, LATEST_TTL.toSeconds());
//            }
//            return null;
//        });
//    }
//}
