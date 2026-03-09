package com.example.demo.market_data.infrastructure.cache.redis;

import com.example.demo.infra_shard.redis.RedisKeys;
import com.example.demo.market_data.application.port.out.WriteRedisLatestDataPort;
import com.example.demo.market_data.domain.domain.Tick;
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
public class TickLatestRedisWriteAdapter implements WriteRedisLatestDataPort<Tick> {

    private final RedisTemplate<String, String> redis;

    private final String env ="local";

    // latest tick TTL (너 정책에 맞게 조정)
    private static final Duration LATEST_TTL = Duration.ofSeconds(30);

    // 로우레벨에서는 serializer를 "명시 고정"하는 게 가장 안전
    private static final StringRedisSerializer S = new StringRedisSerializer();

    private static final byte[] F_TS  = S.serialize("ts");
    private static final byte[] F_BID = S.serialize("bid");
    private static final byte[] F_ASK = S.serialize("ask");

    @Override
    public void upsert(Tick t) {
        if (t == null) return;
        upsertAll(List.of(t));
    }

    @Override
    public void upsertAll(List<Tick> ticks) {
        if (ticks == null || ticks.isEmpty()) return;
        redis.executePipelined((RedisCallback<Object>) (RedisConnection connection) -> {
            for (Tick t : ticks) {
                if (t == null) continue;

                byte[] rawKey = S.serialize(RedisKeys.tickLatest(env, t.marketCodeId()));
                final Map<byte[], byte[]> fields = new HashMap<>(5);


                fields.put( F_TS,  S.serialize(Long.toString(t.timestamp())));
                fields.put( F_BID, S.serialize(t.bid().toPlainString()));
                fields.put( F_ASK, S.serialize(t.ask().toPlainString()));

                connection.hashCommands().hMSet(rawKey,fields);
                connection.keyCommands().expire(rawKey, LATEST_TTL.toSeconds());
            }
            return null;
        });
    }

}