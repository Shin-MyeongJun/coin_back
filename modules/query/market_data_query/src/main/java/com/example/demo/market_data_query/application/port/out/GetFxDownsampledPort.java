package com.example.demo.market_data_query.application.port.out;

import com.example.demo.market_data_query.application.dto.FxView;

import java.util.List;

public interface GetFxDownsampledPort {
    List<FxView> findDownsampled(String baseCurrency, String quoteCurrency, int bucketSeconds, Long fromTs, Long toTs);
}
