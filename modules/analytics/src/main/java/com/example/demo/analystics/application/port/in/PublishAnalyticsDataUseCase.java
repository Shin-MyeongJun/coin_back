package com.example.demo.analystics.application.port.in;

public interface PublishAnalyticsDataUseCase<DOMAIN> {
    void publish(DOMAIN domain);
}
