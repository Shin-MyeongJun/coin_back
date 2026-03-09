package com.example.demo.contracts.message.meta;

public record ExchangeMessage(
        Long id,
        String name,
        String type,
        String quote
) {

}
