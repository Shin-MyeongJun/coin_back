package com.example.demo.meta_data_query.application.usecase;

import com.example.demo.meta_data_query.application.dto.MarketCodeSearchResult;
import com.example.demo.meta_data_query.application.port.out.SearchMarketCodePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchMarketCodeUseCase {

    private final SearchMarketCodePort port;

    public List<MarketCodeSearchResult> execute(String keyword, Long exchangeId) {
        return port.search(keyword, exchangeId);
    }
}
