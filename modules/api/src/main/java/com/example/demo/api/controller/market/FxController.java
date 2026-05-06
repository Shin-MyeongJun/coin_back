package com.example.demo.api.controller.market;

import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.usecase.GetFxDownsampledUseCase;
import com.example.demo.market_data_query.application.usecase.GetFxRawUseCase;
import com.example.demo.market_data_query.application.usecase.GetLatestFxUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/market/fx")
@RequiredArgsConstructor
public class FxController {

    private final GetLatestFxUseCase getLatestFxUseCase;
    private final GetFxRawUseCase getFxRawUseCase;
    private final GetFxDownsampledUseCase getFxDownsampledUseCase;

    @GetMapping("/latest")
    public FxView getLatest(
            @RequestParam String baseCurrency,
            @RequestParam String quoteCurrency) {
        return getLatestFxUseCase.execute(baseCurrency, quoteCurrency)
                .orElseThrow(() -> new NoSuchElementException("No FX rate found for " + baseCurrency + "/" + quoteCurrency));
    }

    @GetMapping("/raw")
    public List<FxView> getRaw(
            @RequestParam String baseCurrency,
            @RequestParam String quoteCurrency,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return getFxRawUseCase.execute(baseCurrency, quoteCurrency, fromTs, toTs);
    }

    @GetMapping("/downsampled")
    public List<FxView> getDownsampled(
            @RequestParam String baseCurrency,
            @RequestParam String quoteCurrency,
            @RequestParam int bucketSeconds,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return getFxDownsampledUseCase.execute(baseCurrency, quoteCurrency, bucketSeconds, fromTs, toTs);
    }
}
