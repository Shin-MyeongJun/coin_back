package com.example.demo.api.stream.sink;

import com.example.demo.contracts.message.candle.TickCandleMessage;
import com.example.demo.contracts.message.candle.PremiumCandleMessage;
import com.example.demo.contracts.message.candle.PremiumDetailCandleMessage;
import com.example.demo.contracts.message.trade_indicator.TickIndicatorMessage;
import com.example.demo.contracts.message.trade_indicator.PremiumIndicatorMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class AnalyticsStream {

    public final Sinks.Many<TickCandleMessage> tickCandleSink = Sinks.many().multicast().onBackpressureBuffer();
    public final Sinks.Many<PremiumCandleMessage> premiumCandleSink = Sinks.many().multicast().onBackpressureBuffer();
    public final Sinks.Many<PremiumDetailCandleMessage> premiumDetailCandleSink = Sinks.many().multicast().onBackpressureBuffer();
    public final Sinks.Many<TickIndicatorMessage> tickIndicatorSink = Sinks.many().multicast().onBackpressureBuffer();
    public final Sinks.Many<PremiumIndicatorMessage> premiumIndicatorSink = Sinks.many().multicast().onBackpressureBuffer();
}
