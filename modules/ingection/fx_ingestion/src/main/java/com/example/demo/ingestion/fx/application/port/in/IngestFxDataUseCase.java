package com.example.demo.ingestion.fx.application.port.in;


import com.example.demo.contracts.message.fx.FxMessage;

public interface IngestFxDataUseCase {
    FxMessage get(String quote , String base);
}
