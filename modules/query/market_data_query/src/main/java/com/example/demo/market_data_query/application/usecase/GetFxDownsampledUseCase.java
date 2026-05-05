package com.example.demo.market_data_query.application.usecase;

import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.port.out.GetFxDownsampledPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetFxDownsampledUseCase {

    private final GetFxDownsampledPort port;

    public List<FxView> execute(String baseCurrency, String quoteCurrency, int bucketSeconds, Long fromTs, Long toTs) {
        return port.findDownsampled(baseCurrency, quoteCurrency, bucketSeconds, fromTs, toTs);
    }
}
