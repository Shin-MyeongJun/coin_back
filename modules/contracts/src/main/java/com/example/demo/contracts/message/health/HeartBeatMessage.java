package com.example.demo.contracts.message.health;

public record HeartBeatMessage(
        String moduleName,
        String  uuid
) {
}
