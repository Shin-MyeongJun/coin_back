package application.port.out;

import java.util.List;

public interface FlushAndSaveEconomicValuePort <ENTITY> {
    void flush();
    void saveAll(List<ENTITY> list);
}
