package com.example.demo.analystics.domain.domain.indicator.open.state;

public sealed interface IndicatorState permits EmaState,RsiState,TrState,MeanState,StddevState {
}
