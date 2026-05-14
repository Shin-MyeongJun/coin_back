package com.example.demo.api.controller.market;

import com.example.demo.api.common.CursorPage;
import com.example.demo.infra_shard.paging.CursorDirection;
import com.example.demo.infra_shard.paging.CursorSlice;
import com.example.demo.market_data_query.application.dto.PremiumRankingView;
import com.example.demo.market_data_query.application.dto.PremiumSnapshotView;
import com.example.demo.market_data_query.application.dto.PremiumTimeSeriesView;
import com.example.demo.market_data_query.application.usecase.GetPremiumRankingUseCase;
import com.example.demo.market_data_query.application.usecase.GetPremiumSnapshotByBaseUseCase;
import com.example.demo.market_data_query.application.usecase.GetPremiumTimeSeriesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/market/premium")
@RequiredArgsConstructor
public class PremiumController {

    private final GetPremiumSnapshotByBaseUseCase getPremiumSnapshotByBaseUseCase;
    private final GetPremiumTimeSeriesUseCase getPremiumTimeSeriesUseCase;
    private final GetPremiumRankingUseCase getPremiumRankingUseCase;

    @GetMapping("/snapshot/{base}")
    public List<PremiumSnapshotView> getSnapshot(@PathVariable String base) {
        return getPremiumSnapshotByBaseUseCase.execute(base);
    }

    @GetMapping("/series")
    public List<PremiumTimeSeriesView> getSeries(
            @RequestParam Long baseExchangeId,
            @RequestParam Long compareExchangeId,
            @RequestParam String symbol,
            @RequestParam int bucketSeconds,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return getPremiumTimeSeriesUseCase.execute(baseExchangeId, compareExchangeId, symbol, bucketSeconds, fromTs, toTs);
    }

    @GetMapping("/series/cursor")
    public CursorPage<PremiumTimeSeriesView> getSeriesCursor(
            @RequestParam Long baseExchangeId,
            @RequestParam Long compareExchangeId,
            @RequestParam String symbol,
            @RequestParam int bucketSeconds,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "200") int limit,
            @RequestParam(required = false) String direction) {
        CursorSlice<PremiumTimeSeriesView> slice = getPremiumTimeSeriesUseCase.executeCursor(
                baseExchangeId, compareExchangeId, symbol, bucketSeconds, cursor, limit,
                CursorDirection.parseOrDefault(direction));
        return new CursorPage<>(slice.items(), slice.nextCursor(), slice.hasMore());
    }

    @GetMapping("/ranking")
    public List<PremiumRankingView> getRanking(
            @RequestParam(defaultValue = "10") int n) {
        return getPremiumRankingUseCase.execute(n);
    }
}
