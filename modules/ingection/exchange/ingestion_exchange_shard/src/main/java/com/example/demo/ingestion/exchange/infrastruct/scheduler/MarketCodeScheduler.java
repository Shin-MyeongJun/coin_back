package com.example.demo.ingestion.exchange.infrastruct.scheduler;


import com.example.demo.ingestion.exchange.application.port.in.HandleMetaRawUseCase;
import com.example.demo.ingestion.exchange.application.port.out.PublishRawSchedulePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketCodeScheduler implements PublishRawSchedulePort {
    private final HandleMetaRawUseCase handler;

    @Override
    @Scheduled(fixedRate = 1000*60*10)
    public void process(){
        log.info("meta raw update handling....");
        handler.process();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("meta raw init data handling....");
        handler.process();
    }
}
