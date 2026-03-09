package com.example.demo.ingestion.fx.infrastructure.client;

import com.example.demo.ingestion.fx.application.port.in.GetFxRawUseCase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Component
public class NaverRealTimeFxClient implements GetFxRawUseCase {

    private static final String V1_URL =
            "https://m.search.naver.com/p/csearch/content/qapirender.nhn" +
                    "?key=calculator&pkid=141&q=%ED%99%98%EC%9C%A8&where=m" +
                    "&u1=keb&u6=standardUnit&u7=0&u8=down&u2=1" +
                    "&u3={base}&u4={quote}";

    private final WebClient webClient;

    public NaverRealTimeFxClient(WebClient.Builder builder) {
        this.webClient = builder
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0") // 최소 방어
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE + "," + MediaType.TEXT_HTML_VALUE)
                .build();
    }

    private String buildUrl(String base, String quote) {
        return UriComponentsBuilder.fromUriString(V1_URL)
                .buildAndExpand(Map.of("base", base, "quote", quote))
                .encode(StandardCharsets.UTF_8)
                .toUriString();
    }

    @Override
    public Mono<String> getRaw(String base, String quote) {
        String target = buildUrl(base, quote);

        return webClient.get()
                .uri(target)
                .retrieve()
                .onStatus(s -> s.value() == 429, r -> Mono.error(new RuntimeException("NAVER_RATE_LIMIT")))
                .onStatus(s -> s.is4xxClientError() || s.is5xxServerError(),
                        r -> r.bodyToMono(String.class).defaultIfEmpty("").map(body ->
                                new RuntimeException("NAVER_HTTP_" + r.statusCode().value() + " body=" + body)))
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(2)); // “늦으면 무가치” 정책에 맞춰 짧게
    }
}