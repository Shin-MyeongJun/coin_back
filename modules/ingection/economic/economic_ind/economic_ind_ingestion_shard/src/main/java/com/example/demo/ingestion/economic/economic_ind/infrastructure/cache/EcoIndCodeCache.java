package com.example.demo.ingestion.economic.economic_ind.infrastructure.cache;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicIndicatorCode;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EcoIndCodeCache {
    Map<EconomicIndicatorCode,Long> indCodeIdCache = new ConcurrentHashMap<>();
    Map<Long,EconomicIndicatorCode> indCodeCache = new ConcurrentHashMap<>();

    public void put(EconomicIndicatorCode indCode,Long id) {
        indCodeIdCache.put(indCode,id);
        indCodeCache.put(id,indCode);
    }
    public void put(Map<Long,EconomicIndicatorCode> indCodes) {
        indCodeCache.putAll(indCodes);
        indCodes.forEach((id, code) -> indCodeIdCache.put(code, id));
    }


    public Long getId(EconomicIndicatorCode indCode) {
        return indCodeIdCache.get(indCode);
    }
    public EconomicIndicatorCode  getIndCode(Long id) {
        return indCodeCache.get(id);
    }

}
