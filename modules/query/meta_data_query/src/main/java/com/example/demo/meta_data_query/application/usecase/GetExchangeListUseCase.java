package com.example.demo.meta_data_query.application.usecase;

import com.example.demo.meta_data_query.application.dto.ExchangeView;
import com.example.demo.meta_data_query.application.port.out.GetExchangeListPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetExchangeListUseCase {

    private final GetExchangeListPort port;

    public List<ExchangeView> execute() {
        return port.findAll();
    }
}
