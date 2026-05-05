package com.example.demo.market_data_query.application.usecase;

import com.example.demo.market_data_query.application.dto.TickLatestView;
import com.example.demo.market_data_query.application.port.out.GetLatestTickPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetLatestTickUseCase {

    private final GetLatestTickPort port;

    public Optional<TickLatestView> execute(Long marketCodeId) {
        return port.findByMarketCodeId(marketCodeId);
    }
}
