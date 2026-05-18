package com.example.demo.api.stream.controller;

import com.example.demo.api.config.CorsConfig;
import com.example.demo.api.config.SecurityConfig;
import com.example.demo.api.config.security.SecurityTestSupport;
import com.example.demo.api.config.security.filter.JwtPrincipal;
import com.example.demo.api.config.security.filter.PrincipalSupport;
import com.example.demo.api.stream.handler.CandleCloseSseHandler;
import com.example.demo.api.stream.handler.IndicatorCloseSseHandler;
import com.example.demo.api.stream.handler.PremiumDetailSseHandler;
import com.example.demo.api.stream.handler.PremiumSseHandler;
import com.example.demo.api.stream.handler.TickSseHandler;
import com.example.demo.user.domain.domain.AccountId;
import com.example.demo.user.domain.domain.AccountTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StreamController.class)
@Import({CorsConfig.class, SecurityConfig.class, SecurityTestSupport.class})
class StreamControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean TickSseHandler tickSseHandler;
    @MockitoBean PremiumSseHandler premiumSseHandler;
    @MockitoBean PremiumDetailSseHandler premiumDetailSseHandler;
    @MockitoBean CandleCloseSseHandler candleCloseSseHandler;
    @MockitoBean IndicatorCloseSseHandler indicatorCloseSseHandler;

    @Test
    @DisplayName("GET /stream/ticks?marketCodeId=100 — tickSseHandler.subscribe(100L) 위임")
    void streamTicks_withMarketCodeId_delegatesToHandler() throws Exception {
        // given
        given(tickSseHandler.subscribe(100L)).willReturn(new SseEmitter());

        // when / then
        mvc.perform(get("/api/v1/stream/ticks")
                        .param("marketCodeId", "100")
                        .with(authenticatedStream())
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());

        then(tickSseHandler).should().subscribe(100L);
    }

    @Test
    @DisplayName("GET /stream/ticks — marketCodeId 없으면 subscribe(null) 위임")
    void streamTicks_noMarketCodeId_delegatesWithNull() throws Exception {
        // given
        given(tickSseHandler.subscribe(null)).willReturn(new SseEmitter());

        // when / then
        mvc.perform(get("/api/v1/stream/ticks")
                        .with(authenticatedStream())
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());

        then(tickSseHandler).should().subscribe(null);
    }

    @Test
    @DisplayName("GET /stream/premium — premiumSseHandler.subscribe() 위임")
    void streamPremium_delegatesToHandler() throws Exception {
        // given
        given(premiumSseHandler.subscribe()).willReturn(new SseEmitter());

        // when / then
        mvc.perform(get("/api/v1/stream/premium")
                        .with(authenticatedStream())
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());

        then(premiumSseHandler).should().subscribe();
    }

    @Test
    @DisplayName("GET /stream/premium-detail/raw ??premiumDetailSseHandler.subscribe() ?꾩엫")
    void streamPremiumDetailRaw_delegatesToHandler() throws Exception {
        // given
        given(premiumDetailSseHandler.subscribe()).willReturn(new SseEmitter());

        // when / then
        mvc.perform(get("/api/v1/stream/premium-detail/raw")
                        .with(authenticatedStream())
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());

        then(premiumDetailSseHandler).should().subscribe();
    }

    @Test
    @DisplayName("GET /stream/candles/close?type=premium — candleCloseSseHandler.subscribe(\"premium\") 위임")
    void streamCandleClose_withType_delegatesToHandler() throws Exception {
        // given
        given(candleCloseSseHandler.subscribe("premium")).willReturn(new SseEmitter());

        // when / then
        mvc.perform(get("/api/v1/stream/candles/close")
                        .param("type", "premium")
                        .with(authenticatedStream())
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());

        then(candleCloseSseHandler).should().subscribe("premium");
    }

    @Test
    @DisplayName("GET /stream/candles/close?type=premium-detail ??candleCloseSseHandler.subscribe(\"premium-detail\") ?꾩엫")
    void streamPremiumDetailCandleClose_delegatesToHandler() throws Exception {
        // given
        given(candleCloseSseHandler.subscribe("premium-detail")).willReturn(new SseEmitter());

        // when / then
        mvc.perform(get("/api/v1/stream/candles/close")
                        .param("type", "premium-detail")
                        .with(authenticatedStream())
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());

        then(candleCloseSseHandler).should().subscribe("premium-detail");
    }

    @Test
    @DisplayName("GET /stream/indicators/close — 기본값 type=\"tick\" 으로 위임")
    void streamIndicatorClose_defaultType_delegatesToHandler() throws Exception {
        // given
        given(indicatorCloseSseHandler.subscribe("tick")).willReturn(new SseEmitter());

        // when / then
        mvc.perform(get("/api/v1/stream/indicators/close")
                        .with(authenticatedStream())
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk());

        then(indicatorCloseSseHandler).should().subscribe("tick");
    }

    private static RequestPostProcessor authenticatedStream() {
        return request -> {
            PrincipalSupport.store(request, new JwtPrincipal(AccountId.generate(), AccountTier.FREE));
            return request;
        };
    }
}
