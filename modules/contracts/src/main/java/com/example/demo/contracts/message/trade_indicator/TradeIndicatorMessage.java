package com.example.demo.contracts.message.trade_indicator;

public sealed interface TradeIndicatorMessage
        permits TickIndicatorMessage, PremiumIndicatorMessage {
}
