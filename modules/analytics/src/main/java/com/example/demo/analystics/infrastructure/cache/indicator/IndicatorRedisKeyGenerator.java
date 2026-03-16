package com.example.demo.analystics.infrastructure.cache.indicator;

import org.springframework.stereotype.Component;

@Component
public class IndicatorRedisKeyGenerator {
    private static String base(String env) {
        return "ys:" + env + ":v1";
    }

    // Indicator state (tf + type 필수)
    public  String tickIndicatorState(String env,int partitionId,String tf) {
        return base(env) + ":tick:indicator:state:"+partitionId +":"+ tf;
    }

    public  String premiumIndicatorState(String env, int partitionId, String tf) {
        return base(env) + ":premium:indicator:state:"+partitionId +":"+ tf;
    }
}
