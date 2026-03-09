package com.example.demo.infre_exchange.upbit.util;


import io.micrometer.common.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
public class UpbitAuthenticationFilter implements ExchangeFilterFunction {
    private final UpbitAuthTokenProvider tokenProvider;

    public UpbitAuthenticationFilter(UpbitAuthTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Mono<ClientResponse> filter(@NonNull ClientRequest request,  @NonNull ExchangeFunction next) {
        String path = request.url().getPath();

        boolean isPrivate =
               path.startsWith("/v1/accounts")
            || path.startsWith("/v1/orders")
            || path.startsWith("/v1/withdraws")
            || path.startsWith("/v1/deposits");

        if (!isPrivate) {
            return next.exchange(request);
        }

        // 쿼리스트링이 있으면 가져오고, 없으면 빈 문자열
        String qs = request.url().getRawQuery();
        String jwt = tokenProvider.createToken(qs);

        // 원본 요청에 Authorization 헤더 추가
        ClientRequest authReq = ClientRequest.from(request)
            .header("Authorization", "Bearer " + jwt)
            .build();

        return next.exchange(authReq);
    }
}