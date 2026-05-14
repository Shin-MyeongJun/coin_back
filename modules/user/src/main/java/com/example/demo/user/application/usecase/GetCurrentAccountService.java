package com.example.demo.user.application.usecase;

import com.example.demo.user.application.port.in.GetCurrentAccountQuery;
import com.example.demo.user.application.port.out.LoadAccountPort;
import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCurrentAccountService implements GetCurrentAccountQuery {

    private final LoadAccountPort loadAccountPort;

    @Override
    @Transactional(readOnly = true)
    public Account get(AccountId accountId) {
        return loadAccountPort.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
