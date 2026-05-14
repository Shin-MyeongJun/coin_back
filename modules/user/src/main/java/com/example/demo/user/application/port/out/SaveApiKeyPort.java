package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.ApiKey;

public interface SaveApiKeyPort {
    ApiKey save(ApiKey apiKey);
}
