package com.example.demo.market_data_query.application.port.out;

import com.example.demo.market_data_query.application.dto.TickBulkView;

import java.util.List;

public interface GetLatestTickBulkPort {
    List<TickBulkView> findByMarketCodeIds(List<Long> marketCodeIds);
}
