package com.example.demo.api.controller.economic;

import com.example.demo.economic_query.application.dto.IndicatorChangeRateView;
import com.example.demo.economic_query.application.dto.IndicatorMetaView;
import com.example.demo.economic_query.application.dto.IndicatorSeriesView;
import com.example.demo.economic_query.application.usecase.GetIndicatorChangeRateUseCase;
import com.example.demo.economic_query.application.usecase.GetIndicatorListByCategoryUseCase;
import com.example.demo.economic_query.application.usecase.GetIndicatorMetaUseCase;
import com.example.demo.economic_query.application.usecase.GetIndicatorSeriesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/economic/indicators")
@RequiredArgsConstructor
public class IndicatorController {

    private final GetIndicatorSeriesUseCase getIndicatorSeriesUseCase;
    private final GetIndicatorMetaUseCase getIndicatorMetaUseCase;
    private final GetIndicatorListByCategoryUseCase getIndicatorListByCategoryUseCase;
    private final GetIndicatorChangeRateUseCase getIndicatorChangeRateUseCase;

    @GetMapping("/{codeId}/series")
    public List<IndicatorSeriesView> getSeries(
            @PathVariable Long codeId,
            @RequestParam Long fromTs,
            @RequestParam Long toTs) {
        return getIndicatorSeriesUseCase.execute(codeId, fromTs, toTs);
    }

    @GetMapping("/{codeId}")
    public IndicatorMetaView getMeta(@PathVariable Long codeId) {
        return getIndicatorMetaUseCase.executeById(codeId)
                .orElseThrow(() -> new NoSuchElementException("Indicator not found: " + codeId));
    }

    @GetMapping
    public List<IndicatorMetaView> listByCategory(@RequestParam(required = false) String category) {
        if (category != null) {
            return getIndicatorListByCategoryUseCase.execute(category);
        }
        return getIndicatorMetaUseCase.executeAll();
    }

    @GetMapping("/{codeId}/change-rate")
    public List<IndicatorChangeRateView> getChangeRate(@PathVariable Long codeId) {
        return getIndicatorChangeRateUseCase.execute(codeId);
    }
}
