package com.example.demo.api.controller.market;

import com.example.demo.market_data_query.application.dto.TickBulkView;
import com.example.demo.market_data_query.application.dto.TickLatestView;
import com.example.demo.api.config.CorsConfig;
import com.example.demo.api.config.SecurityConfig;
import com.example.demo.market_data_query.application.usecase.GetLatestTickBulkUseCase;
import com.example.demo.market_data_query.application.usecase.GetLatestTickUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TickController.class)
@Import({CorsConfig.class, SecurityConfig.class})
class TickControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean GetLatestTickUseCase getLatestTickUseCase;
    @MockitoBean GetLatestTickBulkUseCase getLatestTickBulkUseCase;

    @Test
    @DisplayName("GET /latest/{marketCodeId} — 200 OK + TickLatestView 반환")
    void getLatest_found_returns200WithBody() throws Exception {
        // given
        TickLatestView view = new TickLatestView(1L, 100L, 1_000L, new BigDecimal("50000"), new BigDecimal("50001"));
        given(getLatestTickUseCase.execute(100L)).willReturn(Optional.of(view));

        // when / then
        mvc.perform(get("/api/v1/market/ticks/latest/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.marketCodeId").value(100))
                .andExpect(jsonPath("$.bid").value(50000));
    }

    @Test
    @DisplayName("GET /latest/{marketCodeId} — tick 없으면 404 ProblemDetail")
    void getLatest_notFound_returns404ProblemDetail() throws Exception {
        // given
        given(getLatestTickUseCase.execute(999L)).willReturn(Optional.empty());

        // when / then
        mvc.perform(get("/api/v1/market/ticks/latest/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("999")));
    }

    @Test
    @DisplayName("GET /latest?marketCodeIds=1,2 — 200 OK + bulk 목록 반환")
    void getLatestBulk_validIds_returns200() throws Exception {
        // given
        TickBulkView v1 = new TickBulkView(1L, 1_000L, new BigDecimal("50000"), new BigDecimal("50001"));
        TickBulkView v2 = new TickBulkView(2L, 1_000L, new BigDecimal("3000"), new BigDecimal("3001"));
        given(getLatestTickBulkUseCase.execute(List.of(1L, 2L))).willReturn(List.of(v1, v2));

        // when / then
        mvc.perform(get("/api/v1/market/ticks/latest").param("marketCodeIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].marketCodeId").value(1))
                .andExpect(jsonPath("$[1].marketCodeId").value(2));
    }

    @Test
    @DisplayName("GET /latest?marketCodeIds=1,abc — 비정수값은 400 ProblemDetail")
    void getLatestBulk_invalidId_returns400() throws Exception {
        // when / then
        mvc.perform(get("/api/v1/market/ticks/latest").param("marketCodeIds", "1,abc"))
                .andExpect(status().isBadRequest());
    }
}
