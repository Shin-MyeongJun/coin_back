package application.port.out;

import java.util.List;

public interface LoadRawIndSchedulePort<RAW> {
    List<RAW> getRawSchedules();
}
