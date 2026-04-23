package application.port.out;

import java.util.List;

public interface LoadRawIndDataPort<RAW> {
    List<RAW> getRaws();
    RAW getRaw(String target);
}
