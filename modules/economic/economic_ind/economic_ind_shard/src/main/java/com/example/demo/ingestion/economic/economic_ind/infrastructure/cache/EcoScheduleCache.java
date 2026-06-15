package com.example.demo.ingestion.economic.economic_ind.infrastructure.cache;

import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EcoScheduleCache {
    Map<EconomicSchedule,Long> ScheduleIdCache = new ConcurrentHashMap<>();
    Map<Long,EconomicSchedule>  ScheduleCache = new ConcurrentHashMap<>();

    public void put(EconomicSchedule schedule,Long id) {
        ScheduleIdCache.put(schedule,id);
        ScheduleCache.put(id,schedule);
    }
    public void put(Map<Long,EconomicSchedule> schedules) {
        ScheduleCache.putAll(schedules);
        schedules.forEach((id, code) -> ScheduleIdCache.put(code, id));
    }


    public Long getId(EconomicSchedule schedule) {
        return ScheduleIdCache.get(schedule);
    }
    public EconomicSchedule  getSchedule(Long id) {
        return ScheduleCache.get(id);
    }
    public List<EconomicSchedule> getAllSchedule() {
        return ScheduleCache.values().stream().toList();
    }

}
