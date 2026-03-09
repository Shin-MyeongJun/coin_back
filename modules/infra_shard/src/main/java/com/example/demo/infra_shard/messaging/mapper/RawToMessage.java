package com.example.demo.infra_shard.messaging.mapper;

import java.util.Map;

public interface RawToMessage<RAW,MESSAGE>{
    MESSAGE toMessage(RAW raw, Map<String,String> args);
}
