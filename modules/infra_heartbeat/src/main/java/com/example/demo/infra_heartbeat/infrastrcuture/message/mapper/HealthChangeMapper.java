package com.example.demo.infra_heartbeat.infrastrcuture.message.mapper;

import com.example.demo.contracts.message.health.HealthChangeMessage;
import com.example.demo.infra_heartbeat.domain.Health;
import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.HealthStatus;
import com.example.demo.infra_heartbeat.domain.HealthValue;
import com.example.demo.infra_heartbeat.domain.ModuleName;
import com.example.demo.infra_shard.messaging.mapper.MessageToDomain;
import org.springframework.stereotype.Component;

@Component
public class HealthChangeMapper implements MessageToDomain<HealthChangeMessage, Health> {

    @Override
    public Health toDomain(HealthChangeMessage hcm) {
        HealthMeta meta = new HealthMeta(
                ModuleName.from(hcm.moduleName()),
                hcm.subType(),
                hcm.uuid()
        );
        HealthValue value = new HealthValue(
                HealthStatus.valueOf(hcm.currentCondition()),
                System.currentTimeMillis(),
                0L
        );

        return new Health(meta,value);
    }
}
