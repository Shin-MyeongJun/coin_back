package com.example.demo.analystics.infrastructure.cache.candle;

import org.springframework.stereotype.Component;

@Component
public class CandleRedisKeyGenerator {
    private static String base(String env) {
        return "ys:" + env + ":v1";
    }

    public  String tickCandleState(String env , int partitionId, String tf) {
        return base(env) + ":tick:candle:state:"+partitionId +":"+ tf;
    }

    public  String premiumCandleState(String env ,int partitionId, String tf) {
        return base(env) + ":premium:candle:state:"+partitionId +":"+ tf;
    }

    public String premiumDetailCandleState(String env,int partitionId, String tf) {
        return base(env) + ":premium:detail:candle:state:"+partitionId +":"+ tf;
    }

}
