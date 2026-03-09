package com.example.demo.infra_heartbeat.infrastrcuture.message.mapper;

import com.example.demo.contracts.message.health.HeartBeatMessage;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.ModuleName;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import org.springframework.stereotype.Component;

@Component
public class HeartBeatMapper implements MessageToDomain<HeartBeatMessage, HealthMeta> {
    @Override
    public HealthMeta toDomain(HeartBeatMessage hbm) {
        return new HealthMeta(
                ModuleName.valueOf(hbm.moduleName()),
                hbm.uuid()
        );
    }
}
