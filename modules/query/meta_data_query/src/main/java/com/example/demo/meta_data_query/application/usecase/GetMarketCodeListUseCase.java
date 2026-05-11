package com.example.demo.meta_data_query.application.usecase;

import com.example.demo.meta_data_query.application.dto.MarketCodeView;
import com.example.demo.meta_data_query.application.port.out.GetMarketCodeListPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMarketCodeListUseCase {

    private final GetMarketCodeListPort port;

    public List<MarketCodeView> execute() {
        return port.findAll();
    }
}
