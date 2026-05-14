package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.PrefixAndSecret;

public interface ApiKeySecretGeneratorPort {
    PrefixAndSecret generate();
}
