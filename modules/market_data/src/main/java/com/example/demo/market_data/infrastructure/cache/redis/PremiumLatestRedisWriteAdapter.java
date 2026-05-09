package com.example.demo.market_data.infrastructure.cache.redis;

import com.example.demo.infra_shard.redis.RedisKeys;
import com.example.demo.market_data.application.port.out.WriteRedisLatestDataPort;
import com.example.demo.market_data.domain.domain.Premium;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class PremiumLatestRedisWriteAdapter implements WriteRedisLatestDataPort<Premium> {

    private final RedisTemplate<String, String> redis;

    @Value("${app.env:local}") private String env;
    private static final Duration LATEST_TTL = Duration.ofSeconds(30);

    private static final StringRedisSerializer S = new StringRedisSerializer();

    // field name bytes (미리 직렬화해서 비용 절감)
    private static final byte[] F_TS       = S.serialize("ts");
    private static final byte[] F_BID = S.serialize("bid");
    private static final byte[] F_ASK = S.serialize("ask");


    @Override
    public void upsert(Premium p) {
        if (p == null) return;
        upsertAll(List.of(p));
    }

    @Override
    public void upsertAll(List<Premium> premiums) {
        if (premiums == null || premiums.isEmpty()) return;

        redis.executePipelined((RedisCallback<Object>) (RedisConnection connection) -> {
            for (Premium p : premiums) {
                if (p == null) continue;

                byte[] rawKey = S.serialize(RedisKeys.premiumLatest(env, p.baseExchangeId(), p.compareExchangeId(), p.symbol()));
                byte[] vTs  = S.serialize(Long.toString(p.timestamp()));
                byte[] vBid = S.serialize(p.bid().toPlainString());
                byte[] vAsk =  S.serialize(p.ask().toPlainString());

                final Map<byte[], byte[]> fields = new HashMap<>(5);
                fields.put(F_TS, vTs);
                fields.put(F_BID, vBid);
                fields.put(F_ASK, vAsk);
                connection.hashCommands().hMSet(rawKey,fields);
                connection.keyCommands().expire(rawKey, LATEST_TTL.toSeconds());
            }
            return null;
        });
    }
}