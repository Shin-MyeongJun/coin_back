package com.example.demo.user.application.port.in;

import com.example.demo.user.domain.domain.Account;
import com.example.demo.user.domain.domain.AccountId;

public interface GetCurrentAccountQuery {
    Account get(AccountId accountId);
}
