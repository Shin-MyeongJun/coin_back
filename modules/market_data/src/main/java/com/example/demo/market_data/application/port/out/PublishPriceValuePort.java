package com.example.demo.market_data.application.port.out;

public interface PublishPriceValuePort<MESSAGE> {
    public void publish(MESSAGE m);
}
