package com.example.demo.meta_data.application.port.out;

public interface PublishMetaPort<MESSAGE> {
    void publish(MESSAGE message);
}
