package com.example.demo.market_data.application.port.in;


// 특정모듈이 죽었을때 대처
public interface HandleHealthDataUseCase {
    void handlePeerAllDead(String subType);
    void handlePeerRecovered(String subType);
}
