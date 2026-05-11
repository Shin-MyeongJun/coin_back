package com.example.demo.analystics.infrastructure.cache.indicator.writer;

import com.example.demo.analystics.infrastructure.cache.CacheExpirationPolicy;
import com.example.demo.analystics.infrastructure.cache.indicator.IndicatorRedisKeyGenerator;
import com.example.demo.analystics.infrastructure.cache.indicator.codec.IndicatorStateCodecManager;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.IndicatorKeyCodec;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PremiumIndicatorStateWriteAdapterTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void makeKey_usesPremiumIndicatorStateKey() {
        PremiumIndicatorStateWriteAdapter sut = new PremiumIndicatorStateWriteAdapter(
                mock(RedisTemplate.class),
                mock(IndicatorStateCodecManager.class),
                mock(IndicatorKeyCodec.class),
                mock(CacheExpirationPolicy.class),
                new IndicatorRedisKeyGenerator()
        );

        String key = sut.makeKey("local", 3, "1m");

        assertThat(key).isEqualTo("ys:local:v1:premium:indicator:state:3:1m");
    }
}
