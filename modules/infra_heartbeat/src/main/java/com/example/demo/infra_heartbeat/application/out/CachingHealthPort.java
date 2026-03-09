package com.example.demo.infra_heartbeat.application.out;

import com.example.demo.infra_heartbeat.domain.HealthMeta;
import com.example.demo.infra_heartbeat.domain.HealthValue;

import java.util.Map;

public interface CachingHealthPort {
    HealthValue get(HealthMeta meta);

    /** 원자적 갱신을 위해 compute 제공 */
    HealthValue compute(HealthMeta meta, java.util.function.BiFunction<HealthMeta, HealthValue, HealthValue> fn);

    Map<HealthMeta, HealthValue> snapshot();
}
