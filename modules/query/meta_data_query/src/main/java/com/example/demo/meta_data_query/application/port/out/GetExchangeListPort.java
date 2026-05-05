package com.example.demo.meta_data_query.application.port.out;

import com.example.demo.meta_data_query.application.dto.ExchangeView;

import java.util.List;

public interface GetExchangeListPort {
    List<ExchangeView> findAll();
}
