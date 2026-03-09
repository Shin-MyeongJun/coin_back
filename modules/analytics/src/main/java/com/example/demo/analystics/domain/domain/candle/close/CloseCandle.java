package com.example.demo.analystics.domain.domain.candle.close;

public sealed interface CloseCandle permits TickCloseCandle,PremiumCloseCandle,PremiumDetailCloseCandle  {
}
