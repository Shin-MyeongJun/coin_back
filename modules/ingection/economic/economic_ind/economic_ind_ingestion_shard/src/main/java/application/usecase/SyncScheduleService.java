package application.usecase;

import application.port.in.SyncScheduleUseCase;
import application.port.out.ReadScheduledEcoPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class SyncScheduleService implements SyncScheduleUseCase {

    private final ReadScheduledEcoPort readPort;



    @Override
    public void sync() {

    }
}
