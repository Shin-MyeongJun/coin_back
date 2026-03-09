package com.example.demo.contracts.message.health;

public record HealthChangeMessage (
    String moduleName,
    String  uuid,
    String prevCondition,
    String currentCondition
){

}
