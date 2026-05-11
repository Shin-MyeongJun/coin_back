package com.example.demo.contracts.message.candle;

public sealed interface CandleMessage
        permits TickCandleMessage, PremiumCandleMessage, PremiumDetailCandleMessage {
}
