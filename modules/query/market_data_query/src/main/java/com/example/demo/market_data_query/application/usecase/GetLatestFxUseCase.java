package com.example.demo.market_data_query.application.usecase;

import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.port.out.GetLatestFxPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetLatestFxUseCase {

    private final GetLatestFxPort port;

    public Optional<FxView> execute(String baseCurrency, String quoteCurrency) {
        return port.findLatest(baseCurrency, quoteCurrency);
    }
}
