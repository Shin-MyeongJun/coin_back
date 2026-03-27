//package com.example.demo.analystics.infrastructure.cache.__sample;
//
//import com.example.demo.analystics.application.port.out.WriteAnalyticsStatePort;
//import com.example.demo.analystics.domain.domain.Interval;
//import com.example.demo.analystics.domain.domain.candle.open.PremiumDetailCandle;
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
//public class PremiumDetailCandleStateWriteAdapter implements WriteAnalyticsStatePort<PremiumDetailCandle> {
//
//    private final RedisTemplate<String, String> redis;
//
//    private final String env = "local";
//    private static final Duration LATEST_TTL = Duration.ofSeconds(30);
//    private static final long TTL_SECONDS = LATEST_TTL.toSeconds();
//
//    private static final StringRedisSerializer S = new StringRedisSerializer();
//
//    // field name bytes (미리 직렬화해서 비용 절감)
//    private static final byte[] F_OPTS = S.serialize("openTs");
//
//    private static final byte[] F_OPEN_BASE          = S.serialize("open_base");
//    private static final byte[] F_OPEN_COMPARE       = S.serialize("open_compare");
//    private static final byte[] F_OPEN_BASE_QUOTE    = S.serialize("open_base_quote");
//    private static final byte[] F_OPEN_COMPARE_QUOTE = S.serialize("open_compare_quote");
//
//    private static final byte[] F_HIGH_BASE          = S.serialize("high_base");
//    private static final byte[] F_HIGH_COMPARE       = S.serialize("high_compare");
//    private static final byte[] F_HIGH_BASE_QUOTE    = S.serialize("high_base_quote");
//    private static final byte[] F_HIGH_COMPARE_QUOTE = S.serialize("high_compare_quote");
//
//    private static final byte[] F_LOW_BASE           = S.serialize("low_base");
//    private static final byte[] F_LOW_COMPARE        = S.serialize("low_compare");
//    private static final byte[] F_LOW_BASE_QUOTE     = S.serialize("low_base_quote");
//    private static final byte[] F_LOW_COMPARE_QUOTE  = S.serialize("low_compare_quote");
//
//    private static final byte[] F_CLOSE_BASE         = S.serialize("close_base");
//    private static final byte[] F_CLOSE_COMPARE      = S.serialize("close_compare");
//    private static final byte[] F_CLOSE_BASE_QUOTE   = S.serialize("close_base_quote");
//    private static final byte[] F_CLOSE_COMPARE_QUOTE= S.serialize("close_compare_quote");
//
//    @Override
//    public void upsertAll(Interval interval, List<PremiumDetailCandle> pdcs) {
//        if (pdcs == null || pdcs.isEmpty()) return;
//
//        final String tf = interval.getPeriod();
//
//        redis.executePipelined((RedisCallback<Object>) (RedisConnection connection) -> {
//            for (PremiumDetailCandle pdc : pdcs) {
//                if (pdc == null) continue;
//
//                final byte[] rawKey = S.serialize(
//                        RedisKeys.premiumDetailCandleState(
//                                env,
//                                pdc.getBaseExchangeId(),
//                                pdc.getCompareExchangeId(),
//                                pdc.getBase(),
//                                tf
//                        )
//                );
//
//                // getter 반복 제거
//                final var open  = pdc.getOpen();
//                final var high  = pdc.getHigh();
//                final var low   = pdc.getLow();
//                final var close = pdc.getClose();
//
//                // 17 fields (openTs + 16 values) → capacity 여유있게
//                final Map<byte[], byte[]> fields = new HashMap<>(24);
//
//                fields.put(F_OPTS, S.serialize(Long.toString(pdc.getOpenTimestamp())));
//
//                fields.put(F_OPEN_BASE,          S.serialize(open.baseVal().toPlainString()));
//                fields.put(F_OPEN_COMPARE,       S.serialize(open.compareVal().toPlainString()));
//                fields.put(F_OPEN_BASE_QUOTE,    S.serialize(open.baseQuoteVal().toPlainString()));
//                fields.put(F_OPEN_COMPARE_QUOTE, S.serialize(open.compareQuoteVal().toPlainString()));
//
//                fields.put(F_HIGH_BASE,          S.serialize(high.baseVal().toPlainString()));
//                fields.put(F_HIGH_COMPARE,       S.serialize(high.compareVal().toPlainString()));
//                fields.put(F_HIGH_BASE_QUOTE,    S.serialize(high.baseQuoteVal().toPlainString()));
//                fields.put(F_HIGH_COMPARE_QUOTE, S.serialize(high.compareQuoteVal().toPlainString()));
//
//                fields.put(F_LOW_BASE,           S.serialize(low.baseVal().toPlainString()));
//                fields.put(F_LOW_COMPARE,        S.serialize(low.compareVal().toPlainString()));
//                fields.put(F_LOW_BASE_QUOTE,     S.serialize(low.baseQuoteVal().toPlainString()));
//                fields.put(F_LOW_COMPARE_QUOTE,  S.serialize(low.compareQuoteVal().toPlainString()));
//
//                fields.put(F_CLOSE_BASE,         S.serialize(close.baseVal().toPlainString()));
//                fields.put(F_CLOSE_COMPARE,      S.serialize(close.compareVal().toPlainString()));
//                fields.put(F_CLOSE_BASE_QUOTE,   S.serialize(close.baseQuoteVal().toPlainString()));
//                fields.put(F_CLOSE_COMPARE_QUOTE,S.serialize(close.compareQuoteVal().toPlainString()));
//
//
//                connection.hashCommands().hMSet(rawKey, fields);
//                connection.keyCommands().expire(rawKey, TTL_SECONDS);
//            }
//            return null;
//        });
//    }
//}
