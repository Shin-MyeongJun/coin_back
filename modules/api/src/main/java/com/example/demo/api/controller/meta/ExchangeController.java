package com.example.demo.api.controller.meta;

import com.example.demo.meta_data_query.application.dto.ExchangeView;
import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.meta_data_query.application.usecase.GetExchangeListUseCase;
import com.example.demo.meta_data_query.application.usecase.GetMarketCodesByExchangeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meta")
@RequiredArgsConstructor
public class ExchangeController {

    private final GetExchangeListUseCase getExchangeListUseCase;
    private final GetMarketCodesByExchangeUseCase getMarketCodesByExchangeUseCase;

    @GetMapping("/exchanges")
    public List<ExchangeView> listExchanges() {
        return getExchangeListUseCase.execute();
    }

    @GetMapping("/exchanges/{exchangeId}/markets")
    public List<MarketCodeView> listMarketsByExchange(@PathVariable Long exchangeId) {
        return getMarketCodesByExchangeUseCase.execute(exchangeId);
    }
}
