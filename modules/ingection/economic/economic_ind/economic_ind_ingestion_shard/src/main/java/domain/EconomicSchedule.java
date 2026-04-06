package domain;


import domain.enums.ScheduleState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class EconomicSchedule {
    private final EconomicIndicatorCode code;//어떤상픔

    private final String releaseDate;

    @Setter
    private ScheduleState state = ScheduleState.PENDING;
    @Setter
    private  Long fetchedAt;//수정시점

    public String getReleaseCode(){
        return code.indicatorCode() +"_"+releaseDate;
    }

}
