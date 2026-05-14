package com.example.demo.user.infrastructure.security;

import com.example.demo.user.application.port.out.ApiKeySecretGeneratorPort;
import com.example.demo.user.domain.domain.ApiKeyPrefix;
import com.example.demo.user.domain.domain.ApiKeySecret;
import com.example.demo.user.domain.domain.PrefixAndSecret;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomApiKeySecretGenerator implements ApiKeySecretGeneratorPort {

    private static final char[] ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int PREFIX_LEN = 8;
    private static final int SECRET_LEN = 32;

    private final SecureRandom random = new SecureRandom();

    @Override
    public PrefixAndSecret generate() {
        return new PrefixAndSecret(
                new ApiKeyPrefix(randomString(PREFIX_LEN)),
                new ApiKeySecret(randomString(SECRET_LEN))
        );
    }

    private String randomString(int length) {
        char[] buf = new char[length];
        for (int i = 0; i < length; i++) {
            buf[i] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return new String(buf);
    }
}
