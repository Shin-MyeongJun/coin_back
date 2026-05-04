package com.example.demo.ingestion.economic.economic_ind.infrastructure.scheduler;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.GetRealTimeEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.out.DynamicSchedulingPort;
import com.example.demo.ingestion.economic.economic_ind.domain.EconomicSchedule;
import com.example.demo.ingestion.economic.economic_ind.infrastructure.cache.EcoScheduleCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@RequiredArgsConstructor
public abstract   class EcoDynamicIndScheduler implements DynamicSchedulingPort {

    private final TaskScheduler taskScheduler;
    private final EcoScheduleCache ecoScheduleCache;
    private final GetRealTimeEcoIndUseCase getTask;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Override
    public void adjustSchedule() {
        scheduledTasks.values().forEach(task -> task.cancel(false));
        scheduledTasks.clear();

        // 2. 캐시에서 스케줄 데이터 읽기
        List<EconomicSchedule> schedules = ecoScheduleCache.getAllSchedule();

        // 3. 동적 스케줄링 등록
        for (EconomicSchedule schedule : schedules) {
            Instant releaseTime = Instant.ofEpochMilli(schedule.getReleaseDate());

            if (releaseTime.isBefore(Instant.now())) {
                continue;
            }

            Runnable task = () -> {
                getTask.process(schedule.getCode().type()); //CHANGE CHECK
            };

            ScheduledFuture<?> future = taskScheduler.schedule(task, releaseTime);
            scheduledTasks.put(schedule.getReleaseCode(), future);
        }

        log.info("스케줄러 갱신 완료: 총 {}개 작업 예약됨", scheduledTasks.size());
    }
}
