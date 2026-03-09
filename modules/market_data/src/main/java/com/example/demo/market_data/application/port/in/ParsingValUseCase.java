package com.example.demo.market_data.application.port.in;

public interface ParsingValUseCase<DOMAIN,VAL> {
     VAL getKey(DOMAIN domain);
}
