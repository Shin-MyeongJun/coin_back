package application.port.out;

import domain.EconomicRawIndicator;
import domain.EconomicSchedule;

import java.util.List;

public interface FredClientPort {
     List<EconomicSchedule> getSchedules(String realTimeStart, String realTimeEnd);
     List<EconomicRawIndicator> getRawIndicators();
     EconomicRawIndicator getRawIndicator(String indicator);
}
