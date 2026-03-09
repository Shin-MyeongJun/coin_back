package com.example.demo.market_data.application.port.in;

public interface ConsumeRawValueUseCase<DOMAIN> {

    void consumeValue(DOMAIN domain);

}
