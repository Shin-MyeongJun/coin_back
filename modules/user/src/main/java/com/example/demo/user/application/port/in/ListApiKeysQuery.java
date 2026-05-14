package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;

import java.util.List;

public interface ListApiKeysQuery {
    List<ApiKey> list(AccountId accountId);
}
