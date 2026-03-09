package com.example.demo.infre_exchange.upbit.client;


import com.example.demo.infre_exchange.upbit.config.UpbitProperties;
import com.example.demo.infre_exchange.upbit.util.UpbitAuthTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UpbitApiConnectorImpl implements UpbitApiConnector {
    private final WebClient client = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(2 * 1024 * 1024)) // 2MB
                    .build())
            .build();
    private final UpbitProperties props;
    private final UpbitAuthTokenProvider authTokenProvider;

    // --- Quotation ---
    @Override
    public Mono<String> getMarketsAll() {
        return client.get()
            .uri( props.getQuotation().getMarketsAll())
            .retrieve()
            .bodyToMono(String.class);

    }

    @Override
    public Mono<String> getTicker(String markets) {
        return client.get()
            .uri(uri -> uri
                .path(props.getQuotation().getTicker())
                .queryParam("markets", markets)
                .build()
            )
            .retrieve()
            .bodyToMono(String.class);
    }

    @Override
    public Mono<String> getOrderbook(String market) {
        return client.get()
            .uri(uri -> uri
                .path(props.getQuotation().getOrderbook())
                .queryParam("markets", market)
                .build()
            )
            .retrieve()
            .bodyToMono(String.class);
    }

    @Override
    public Mono<String> getTradesTicks(String market) {
        return client.get()
            .uri(uri -> uri
                .path(props.getQuotation().getTradesTicks())
                .queryParam("market", market)
                .build()
            )
            .retrieve()
            .bodyToMono(String.class);
    }

    @Override
    public Mono<String> getCandleMinutes(String market, String unit) {
        return client.get()
            .uri(uri -> uri
                .path(props.getQuotation().getCandles().getMinutes())
                .queryParam("market", market)
                .queryParam("unit", unit)
                .build()
            )
            .retrieve()
            .bodyToMono(String.class);
    }

    // --- Account ---
    @Override
    public Mono<String> getAccounts() {
        return client.get()
                .uri( uri -> uri
                    .path(props.getAccounts().getList())
                    .build()
                 )
                .header("Content-Type", "application/json")
                .header("Authorization",getToken(null))
                .retrieve()
                .bodyToMono(String.class);
    }

    // --- Order ---
    @Override
    public Mono<String> createOrder(String bodyJson) {
        return client.post()
            .uri(props.getOrders().getCreate())
            .bodyValue(bodyJson)
            .retrieve()
            .bodyToMono(String.class);
    }

    @Override
    public Mono<String> cancelOrder(String uuid) {
        return client.delete()
            .uri(uri -> uri
                .path(props.getOrders().getCancel())
                .build(uuid)
            )
            .retrieve()
            .bodyToMono(String.class);
    }

    @Override
    public Mono<String> getOrders(String queryParams) {
        return client.get()
            .uri(props.getOrders().getList() + "?" + queryParams)
            .retrieve()
            .bodyToMono(String.class);
    }

    @Override
    public Mono<String> getOrderChance(String queryParams) {
        return client.get()
            .uri(props.getOrders().getChance() + "?" + queryParams)
            .retrieve()
            .bodyToMono(String.class);
    }

    // --- Withdraw ---
    @Override
    public Mono<String> getWithdrawChance(String queryParams) {
        return client.get()
            .uri(props.getWithdraws().getChance() + "?" + queryParams)
            .retrieve()
            .bodyToMono(String.class);
    }

    @Override
    public Mono<String> getWithdraws(String queryParams) {
        return client.get()
            .uri(props.getWithdraws().getList() + "?" + queryParams)
            .retrieve()
            .bodyToMono(String.class);
    }

    @Override
    public Mono<String> createWithdraw(String bodyJson) {
        return client.post()
            .uri(props.getWithdraws().getCreate())
            .bodyValue(bodyJson)
            .retrieve()
            .bodyToMono(String.class);
    }

    // --- Deposit ---
    @Override
    public Mono<String> getDepositChance() {
        return client.get()
            .uri(props.getDeposits().getChance())
            .retrieve()
            .bodyToMono(String.class);
    }

    @Override
    public Mono<String> getDeposits() {
        return client.get()
            .uri(props.getDeposits().getList())
            .retrieve()
            .bodyToMono(String.class);
    }

    private String makeQueryString(Map<String, String> queryParams){
        return "";
    }

    private String getToken(String queryString){
        return authTokenProvider.createToken(queryString);
    }


}
