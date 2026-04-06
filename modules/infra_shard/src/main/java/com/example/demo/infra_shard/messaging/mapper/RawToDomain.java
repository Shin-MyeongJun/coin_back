package com.example.demo.infra_shard.messaging.mapper;

import java.util.Map;

public interface RawToDomain <RAW,DOMAIN>{
    DOMAIN toDomain(RAW raw, Map<String,String> args);
}
