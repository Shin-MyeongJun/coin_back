package com.example.demo.economic_query.application.port.out;

import com.example.demo.economic_query.application.dto.CorrelationResultView;

import java.util.List;

public interface GetCorrelationResultPort {
    List<CorrelationResultView> findByAssetSymbol(String assetSymbol);
}
