package com.example.demo.api.controller.market;

import com.example.demo.market_data_query.application.dto.TickBulkView;
import com.example.demo.market_data_query.application.dto.TickLatestView;
import com.example.demo.market_data_query.application.usecase.GetLatestTickBulkUseCase;
import com.example.demo.market_data_query.application.usecase.GetLatestTickUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/market/ticks")
@RequiredArgsConstructor
public class TickController {

    private final GetLatestTickUseCase getLatestTickUseCase;
    private final GetLatestTickBulkUseCase getLatestTickBulkUseCase;

    @GetMapping("/latest/{marketCodeId}")
    public TickLatestView getLatest(@PathVariable Long marketCodeId) {
        return getLatestTickUseCase.execute(marketCodeId)
                .orElseThrow(() -> new NoSuchElementException("No tick found for marketCodeId: " + marketCodeId));
    }

    @GetMapping("/latest")
    public List<TickBulkView> getLatestBulk(@RequestParam String marketCodeIds) {
        List<Long> ids = Arrays.stream(marketCodeIds.split(","))
                .map(Long::parseLong)
                .toList();
        return getLatestTickBulkUseCase.execute(ids);
    }
}
