package com.example.demo.ingestion.economic.economic_ind.infrastructure.scheduler;

import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoIndUseCase;
import com.example.demo.ingestion.economic.economic_ind.application.port.in.ScheduledEcoScheduleUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class FredIndSchedulerTest {

    @Mock ScheduledEcoIndUseCase indCase;
    @Mock ScheduledEcoScheduleUseCase scheduleCase;

    @Test
    @DisplayName("process — scheduleCase.process() 후 indCase.process() 순서 보장")
    void process_callsScheduleCaseThenIndCase() {
        FredIndScheduler sut = new FredIndScheduler(indCase, scheduleCase);

        sut.process();

        InOrder order = inOrder(scheduleCase, indCase);
        order.verify(scheduleCase).process();
        order.verify(indCase).process();
    }
}
