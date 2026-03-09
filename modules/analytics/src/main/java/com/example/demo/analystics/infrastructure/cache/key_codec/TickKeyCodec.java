package com.example.demo.analystics.infrastructure.cache.key_codec;

import com.example.demo.analystics.domain.domain.key.TickKey;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.DataKeyCodec;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
public class TickKeyCodec implements DataKeyCodec<TickKey> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;

    @Override
    public byte[] encode(TickKey tk) {

        return String.valueOf(tk.MarketCodeId())
                .getBytes(UTF8);
    }

    @Override
    public TickKey decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Cannot decode empty bytes");
        }

        String idStr = new String(bytes, UTF8);
        Long marketCodeId = Long.parseLong(idStr);
        return new TickKey(marketCodeId);
    }
}
