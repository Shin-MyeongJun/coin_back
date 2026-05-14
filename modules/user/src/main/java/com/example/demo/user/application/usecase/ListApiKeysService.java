package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.ListApiKeysQuery;
import com.example.demo.user.application.port.out.LoadApiKeyPort;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.ApiKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListApiKeysService implements ListApiKeysQuery {

    private final LoadApiKeyPort loadApiKeyPort;

    @Override
    @Transactional(readOnly = true)
    public List<ApiKey> list(AccountId accountId) {
        return loadApiKeyPort.findByAccountId(accountId);
    }
}
