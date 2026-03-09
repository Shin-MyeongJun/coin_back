package com.example.demo.infre_exchange.upbit.util;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.demo.infre_exchange.upbit.config.UpbitProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Component
public class UpbitAuthTokenProvider {
    private final Algorithm algorithm;
    private final String accessKey;
    private final String secretKey;

    public UpbitAuthTokenProvider(UpbitProperties props) {
        this.accessKey = props.getAccessKey();
        this.secretKey = props.getSecretKey();
        this.algorithm = Algorithm.HMAC256(secretKey);
    }

    /**
     * 쿼리스트링이 있을 때 SHA512 해시를 같이 담고,
     * 없으면 access_key/nonce 만 담아 서명합니다.
     */
    public String createToken(String queryString) {
        com.auth0.jwt.JWTCreator.Builder builder = JWT.create()
            .withClaim("access_key", accessKey)
            .withClaim("nonce", UUID.randomUUID().toString());

        if (queryString != null && !queryString.isBlank()) {
            String hash = sha512Hex(queryString);
            builder.withClaim("query_hash", hash)
                   .withClaim("query_hash_alg", "SHA512");
        }

        return builder.sign(algorithm);
    }

    private String sha512Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-512 해시 생성 실패", e);
        }
    }
}