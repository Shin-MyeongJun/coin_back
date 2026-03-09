package com.example.demo.market_data.infrastructure.cache.redis;

import com.example.demo.infra_shard.redis.RedisKeys;
import com.example.demo.market_data.application.port.out.WriteRedisLatestDataPort;
import com.example.demo.market_data.domain.domain.PremiumDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PremiumDetailLatestRedisWriteAdapter implements WriteRedisLatestDataPort<PremiumDetail> {

    private final RedisTemplate<String, String> redis;

    private final String env = "local";
    private static final Duration LATEST_TTL = Duration.ofSeconds(30);

    private static final StringRedisSerializer S = new StringRedisSerializer();

    // field bytes
    private static final byte[] F_TS         = S.serialize("ts");
    private static final byte[] F_BASE_BID   = S.serialize("baseBid");
    private static final byte[] F_BASE_ASK   = S.serialize("baseAsk");
    private static final byte[] F_BASE_QUOTE = S.serialize("baseQuote");
    private static final byte[] F_COMP_BID   = S.serialize("compareBid");
    private static final byte[] F_COMP_ASK   = S.serialize("compareAsk");
    private static final byte[] F_COMP_QUOTE = S.serialize("compareQuote");

    @Override
    public void upsert(PremiumDetail pd) {
        if (pd == null) return;
        upsertAll(List.of(pd));
    }

    @Override
    public void upsertAll(List<PremiumDetail> list) {
        if (list == null || list.isEmpty()) return;

        redis.executePipelined((RedisCallback<Object>) (RedisConnection connection) -> {
            for (PremiumDetail pd : list) {
                if (pd == null) continue;
                byte[] rawKey = S.serialize(RedisKeys.premiumDetailLatest(env, pd.baseExchangeId(), pd.compareExchangeId(), pd.symbol()));
                final Map<byte[], byte[]> fields = new HashMap<>(8);

                fields.put(F_TS, S.serialize(Long.toString(pd.timestamp())));
                fields.put(F_BASE_BID, S.serialize(pd.baseBid().toPlainString()));
                fields.put(F_BASE_ASK, S.serialize(pd.baseAsk().toPlainString()));
                fields.put(F_BASE_QUOTE, S.serialize(pd.baseQuoteVal().toPlainString()));

                fields.put(F_COMP_BID, S.serialize(pd.compareBid().toPlainString()));
                fields.put(F_COMP_ASK, S.serialize(pd.compareAsk().toPlainString()));
                fields.put(F_COMP_QUOTE, S.serialize(pd.compareQuoteVal().toPlainString()));

                connection.hashCommands().hMSet(rawKey,fields);
                connection.keyCommands().expire(rawKey, LATEST_TTL.toSeconds());
            }
            return null;
        });
    }
}
