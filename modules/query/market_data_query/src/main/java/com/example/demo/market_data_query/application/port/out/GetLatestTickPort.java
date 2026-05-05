package com.example.demo.market_data_query.application.port.out;

import com.example.demo.market_data_query.application.dto.TickLatestView;

import java.util.Optional;

public interface GetLatestTickPort {
    Optional<TickLatestView> findByMarketCodeId(Long marketCodeId);
}
