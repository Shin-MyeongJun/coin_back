package com.example.demo.ingestion.fx.application.port.out;

import com.example.demo.contracts.message.fx.FxMessage;

public interface PublishFxPort {
    void publish(FxMessage fm);
}
