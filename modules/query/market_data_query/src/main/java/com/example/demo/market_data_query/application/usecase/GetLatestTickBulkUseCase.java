package com.example.demo.market_data_query.application.usecase;

import com.example.demo.market_data_query.application.dto.TickBulkView;
import com.example.demo.market_data_query.application.port.out.GetLatestTickBulkPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetLatestTickBulkUseCase {

    private final GetLatestTickBulkPort port;

    public List<TickBulkView> execute(List<Long> marketCodeIds) {
        return port.findByMarketCodeIds(marketCodeIds);
    }
}
