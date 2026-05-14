package com.example.demo.user.application.port.out;

import com.example.demo.user.domain.domain.Account;

public interface SaveAccountPort {
    Account save(Account account);
}
