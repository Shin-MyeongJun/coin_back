package com.example.demo.contracts.message.health;

public record HealthChangeMessage (
    String moduleName,
    String subType,
    String  uuid,
    String prevCondition,
    String currentCondition
){
    public HealthChangeMessage(String moduleName, String uuid, String prevCondition, String currentCondition) {
        this(moduleName, null, uuid, prevCondition, currentCondition);
    }

}
