package domain;

import java.math.BigDecimal;

public record EconomicIndicatorValue (
    BigDecimal value, //값
    String observationDate, //관측일시
    String releaseDate, //발표일시
    String timestamp  //시각
){

}
