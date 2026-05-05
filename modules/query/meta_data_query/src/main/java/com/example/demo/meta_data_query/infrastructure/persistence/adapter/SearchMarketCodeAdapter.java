package com.example.demo.meta_data_query.infrastructure.persistence.adapter;

import com.example.demo.meta_data_query.application.dto.MarketCodeSearchResult;
import com.example.demo.meta_data_query.application.port.out.SearchMarketCodePort;
import com.example.demo.meta_data_query.infrastructure.persistence.querydsl.MarketCodeQueryDslRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchMarketCodeAdapter implements SearchMarketCodePort {

    private final MarketCodeQueryDslRepository queryDslRepository;

    @Override
    public List<MarketCodeSearchResult> search(String keyword, Long exchangeId) {
        return queryDslRepository.search(keyword, exchangeId);
    }
}
