package com.example.demo.api.controller.market;

import com.example.demo.market_data_query.application.dto.PremiumDetailView;
import com.example.demo.market_data_query.application.dto.PremiumTimeSeriesView;
import com.example.demo.market_data_query.application.usecase.GetPremiumDetailAggUseCase;
import com.example.demo.market_data_query.application.usecase.GetPremiumDetailRawUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market/premium-detail")
@RequiredArgsConstructor
public class PremiumDetailController {

    private final GetPremiumDetailRawUseCase getPremiumDetailRawUseCase;
    private final GetPremiumDetailAggUseCase getPremiumDetailAggUseCase;

    @GetMapping("/raw")
    public List<PremiumDetailView> getRaw(
            @RequestParam Long baseExchangeId,
            @RequestParam Long compareExchangeId,
            @RequestParam String symbol,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return getPremiumDetailRawUseCase.execute(baseExchangeId, compareExchangeId, symbol, fromTs, toTs);
    }

    @GetMapping("/agg")
    public List<PremiumTimeSeriesView> getAgg(
            @RequestParam Long baseExchangeId,
            @RequestParam Long compareExchangeId,
            @RequestParam String symbol,
            @RequestParam int bucketSeconds,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return getPremiumDetailAggUseCase.execute(baseExchangeId, compareExchangeId, symbol, bucketSeconds, fromTs, toTs);
    }
}
