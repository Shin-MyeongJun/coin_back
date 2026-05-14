package com.example.demo.user.application.port.out;

import java.time.Instant;

public interface AccessTokenBlacklistPort {
    void blacklist(String jti, Instant expiresAt);
    boolean isBlacklisted(String jti);
}
