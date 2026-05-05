package com.example.demo.market_data_query.application.port.out;

import com.example.demo.market_data_query.application.dto.FxView;

import java.util.List;

public interface GetFxRawPort {
    List<FxView> findRaw(String baseCurrency, String quoteCurrency, Long fromTs, Long toTs);
}
