package com.example.demo.analystics.infrastructure.cache.key_codec;

import com.example.demo.analystics.domain.domain.key.PremiumKey;
import com.example.demo.analystics.infrastructure.cache.key_codec.base.DataKeyCodec;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
public class PremiumKeyCodec implements DataKeyCodec<PremiumKey> {
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final char SEP = ':';


    @Override
    public byte[] encode(PremiumKey pk) {
        return (pk.base()+ SEP+
                pk.baseExchangeId() + SEP +
                pk.compareExchangeId()
                )
                .getBytes(UTF8);
    }

    @Override
    public PremiumKey decode(byte[] bytes) {
        String str = new String(bytes, UTF8);
        int p1 = str.indexOf(SEP);
        int p2 = str.indexOf(SEP, p1 + 1);

        String base = str.substring(0, p1);
        long baseExchangeId = Long.parseLong(str.substring(p1 + 1, p2));
        long compareExchangeId = Long.parseLong(str.substring(p2 + 1));

        return new PremiumKey(
                base,
                baseExchangeId,
                compareExchangeId
        );
    }
}
