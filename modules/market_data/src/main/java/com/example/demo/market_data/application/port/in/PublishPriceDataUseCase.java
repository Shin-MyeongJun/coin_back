package com.example.demo.market_data.application.port.in;

public interface PublishPriceDataUseCase<DOMAIN> {
    void process(DOMAIN domain);
}
