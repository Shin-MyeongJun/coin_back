package com.example.demo.meta_data_query.application.port.out;

import com.example.demo.meta_data_query.application.dto.MarketCodeView;

import java.util.List;

public interface GetMarketCodeListPort {
    List<MarketCodeView> findAll();
}
