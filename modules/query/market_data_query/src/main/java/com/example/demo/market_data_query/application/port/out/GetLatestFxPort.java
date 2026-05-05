package com.example.demo.market_data_query.application.port.out;

import com.example.demo.market_data_query.application.dto.FxView;

import java.util.Optional;

public interface GetLatestFxPort {
    Optional<FxView> findLatest(String baseCurrency, String quoteCurrency);
}
