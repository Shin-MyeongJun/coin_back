package com.example.demo.meta_data_query.application.port.out;

import com.example.demo.meta_data_query.application.dto.MarketCodeSearchResult;

import java.util.List;

public interface SearchMarketCodePort {
    List<MarketCodeSearchResult> search(String keyword, Long exchangeId);
}
