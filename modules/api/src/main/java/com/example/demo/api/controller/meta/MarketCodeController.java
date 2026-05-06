package com.example.demo.api.controller.meta;

import com.example.demo.meta_data_query.application.dto.MarketCodeSearchResult;
import com.example.demo.meta_data_query.application.usecase.SearchMarketCodeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/meta")
@RequiredArgsConstructor
public class MarketCodeController {

    private final SearchMarketCodeUseCase searchMarketCodeUseCase;

    @GetMapping("/markets/search")
    public List<MarketCodeSearchResult> search(
            @RequestParam String q,
            @RequestParam(required = false) Long exchange) {
        return searchMarketCodeUseCase.execute(q, exchange);
    }
}
