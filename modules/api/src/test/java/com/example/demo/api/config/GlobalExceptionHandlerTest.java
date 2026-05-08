package com.example.demo.api.config;

import com.example.demo.api.config.CorsConfig;
import com.example.demo.api.config.SecurityConfig;
import com.example.demo.market_data_query.application.usecase.GetLatestTickBulkUseCase;
import com.example.demo.market_data_query.application.usecase.GetLatestTickUseCase;
import com.example.demo.api.controller.market.TickController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TickController.class)
@Import({CorsConfig.class, SecurityConfig.class})
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mvc;

    @MockitoBean GetLatestTickUseCase getLatestTickUseCase;
    @MockitoBean GetLatestTickBulkUseCase getLatestTickBulkUseCase;

    @Test
    @DisplayName("NoSuchElementException → 404 ProblemDetail, title=Not Found")
    void handleNotFound_returns404ProblemDetail() throws Exception {
        // given
        given(getLatestTickUseCase.execute(any())).willReturn(Optional.empty());

        // when / then
        mvc.perform(get("/api/v1/market/ticks/latest/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    @DisplayName("IllegalArgumentException (NumberFormatException) → 400 ProblemDetail")
    void handleIllegalArgument_returns400ProblemDetail() throws Exception {
        // NumberFormatException extends IllegalArgumentException
        // when / then
        mvc.perform(get("/api/v1/market/ticks/latest").param("marketCodeIds", "1,not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RuntimeException → 500 ProblemDetail, title=Internal Server Error")
    void handleGeneral_returns500ProblemDetail() throws Exception {
        // given
        given(getLatestTickUseCase.execute(any())).willThrow(new RuntimeException("unexpected"));

        // when / then
        mvc.perform(get("/api/v1/market/ticks/latest/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."));
    }
}
