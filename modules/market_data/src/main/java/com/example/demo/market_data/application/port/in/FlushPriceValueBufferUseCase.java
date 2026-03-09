package com.example.demo.market_data.application.port.in;

public interface FlushPriceValueBufferUseCase {
    void flush();

    interface ForTick extends FlushPriceValueBufferUseCase {}
    interface ForPremium extends FlushPriceValueBufferUseCase {}
    interface ForPremiumDetail extends FlushPriceValueBufferUseCase {}
}
