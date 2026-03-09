package com.example.demo.market_data.infrastructure.runner;


import com.example.demo.market_data.application.port.in.InitializeMarketUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MarketInitRunner implements ApplicationRunner {

    private final List<InitializeMarketUseCase> initializers;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initializers.forEach(InitializeMarketUseCase::run);
    }
}
