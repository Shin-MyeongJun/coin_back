package com.example.demo.infre_exchange.upbit.client;


import reactor.core.publisher.Mono;



public interface UpbitApiConnector {
    // Quotation
    Mono<String> getMarketsAll();
    Mono<String> getTicker(String markets);
    Mono<String> getOrderbook(String market);
    Mono<String> getTradesTicks(String market);
    Mono<String> getCandleMinutes(String market, String unit);

    // Account
    Mono<String> getAccounts();

    // Order
    Mono<String> createOrder(String bodyJson);
    Mono<String> cancelOrder(String uuid);
    Mono<String> getOrders(String queryParams);
    Mono<String> getOrderChance(String queryParams);

    // Withdraw/Deposit
    Mono<String> getWithdrawChance(String queryParams);
    Mono<String> getWithdraws(String queryParams);
    Mono<String> createWithdraw(String bodyJson);
    Mono<String> getDepositChance();
    Mono<String> getDeposits();
}
