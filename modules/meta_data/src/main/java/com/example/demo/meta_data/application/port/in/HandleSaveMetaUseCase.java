package com.example.demo.meta_data.application.port.in;

public interface HandleSaveMetaUseCase<ENTITY> {
    ENTITY handle(ENTITY entity);
}
