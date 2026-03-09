package com.example.demo.infra_heartbeat.infrastrcuture.instance;

import com.example.demo.infra_heartbeat.application.out.GetInstanceIdPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ModuleInstanceIdProvider implements GetInstanceIdPort {

    private final String instanceId;

    public ModuleInstanceIdProvider(@Value("${app.moduleName}") String moduleName) {
        this.instanceId = moduleName + "-" + UUID.randomUUID();
    }

    public String get() {
        return instanceId;
    }
}
