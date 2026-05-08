package com.example.demo.api.controller.meta;

import com.example.demo.meta_data_query.application.dto.ExchangeView;
import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.api.config.CorsConfig;
import com.example.demo.api.config.SecurityConfig;
import com.example.demo.meta_data_query.application.usecase.GetExchangeListUseCase;
import com.example.demo.meta_data_query.application.usecase.GetMarketCodesByExchangeUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExchangeController.class)
@Import({CorsConfig.class, SecurityConfig.class})
class ExchangeControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean GetExchangeListUseCase getExchangeListUseCase;
    @MockitoBean GetMarketCodesByExchangeUseCase getMarketCodesByExchangeUseCase;

    @Test
    @DisplayName("GET /exchanges — 200 OK + 거래소 목록 반환")
    void listExchanges_returns200WithList() throws Exception {
        // given
        ExchangeView upbit = new ExchangeView(1L, "Upbit", "SPOT", "KRW", "KR", "ACTIVE");
        ExchangeView binance = new ExchangeView(2L, "Binance", "SPOT", "USDT", "US", "ACTIVE");
        given(getExchangeListUseCase.execute()).willReturn(List.of(upbit, binance));

        // when / then
        mvc.perform(get("/api/v1/meta/exchanges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Upbit"))
                .andExpect(jsonPath("$[1].name").value("Binance"));
    }

    @Test
    @DisplayName("GET /exchanges — 빈 목록도 200 OK")
    void listExchanges_emptyList_returns200() throws Exception {
        // given
        given(getExchangeListUseCase.execute()).willReturn(List.of());

        // when / then
        mvc.perform(get("/api/v1/meta/exchanges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /exchanges/{id}/markets — 200 OK + 마켓 목록 반환")
    void listMarketsByExchange_returns200WithMarkets() throws Exception {
        // given
        MarketCodeView btcKrw = new MarketCodeView(10L, 1L, "BTC", "KRW", "BTC/KRW");
        given(getMarketCodesByExchangeUseCase.execute(1L)).willReturn(List.of(btcKrw));

        // when / then
        mvc.perform(get("/api/v1/meta/exchanges/1/markets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].base").value("BTC"))
                .andExpect(jsonPath("$[0].tradingPair").value("BTC/KRW"));
    }
}
