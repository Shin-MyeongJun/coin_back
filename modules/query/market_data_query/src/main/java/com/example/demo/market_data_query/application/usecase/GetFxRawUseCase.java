package com.example.demo.market_data_query.application.usecase;

import com.example.demo.market_data_query.application.dto.FxView;
import com.example.demo.market_data_query.application.port.out.GetFxRawPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetFxRawUseCase {

    private final GetFxRawPort port;

    public List<FxView> execute(String baseCurrency, String quoteCurrency, Long fromTs, Long toTs) {
        return port.findRaw(baseCurrency, quoteCurrency, fromTs, toTs);
    }
}
