package com.example.demo.market_data.application.usecase;

import com.example.demo.market_data.application.port.in.InitializeMarketUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InitializeService implements InitializeMarketUseCase {
    @Override
    public void run() {

    }
}
