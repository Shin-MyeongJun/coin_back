package com.example.demo.meta_data_query.application.usecase;

import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.meta_data_query.application.port.out.GetMarketCodesByExchangePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMarketCodesByExchangeUseCase {

    private final GetMarketCodesByExchangePort port;

    public List<MarketCodeView> execute(Long exchangeId) {
        return port.findByExchangeId(exchangeId);
    }
}
