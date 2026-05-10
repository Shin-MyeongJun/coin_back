package com.example.demo.contracts.message.health;

public record HeartBeatMessage(
        String moduleName,
        String subType,
        String  uuid
) {
    public HeartBeatMessage(String moduleName, String uuid) {
        this(moduleName, null, uuid);
    }
}
