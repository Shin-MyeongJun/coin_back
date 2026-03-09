package com.example.demo.analystics.application.port.out;

public interface PublishAnalyticValuePort<MESSAGE> {
    public void publish(MESSAGE message);
}
