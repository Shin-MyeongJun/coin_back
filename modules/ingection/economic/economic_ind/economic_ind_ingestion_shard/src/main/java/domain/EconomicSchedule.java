package domain;


import domain.enums.ScheduleState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class EconomicSchedule {
    private final String releaseCode; //code.indicatorCode() +"_"+releaseDate;

    private final EconomicIndicatorCode code;//어떤상픔

    private final Long releaseDate;

    @Setter
    private ScheduleState state = ScheduleState.PENDING;
    @Setter
    private  Long fetchedAt;//수정시점

}
