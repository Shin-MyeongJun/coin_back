package domain;

import java.math.BigDecimal;

public record EconomicIndicatorValue (
    BigDecimal value, //값
    String observationDate, //관측일시
    Long releaseDate, //발표일시
    Long timestamp  //시각
){

}
