package com.example.demo.api.stream.sink;

import com.example.demo.contracts.message.price_value.PremiumMessage;
import com.example.demo.contracts.message.price_value.PremiumDetailMessage;
import com.example.demo.contracts.message.price_value.TickMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class MarketDataStream {

    public final Sinks.Many<TickMessage> tickSink = Sinks.many().multicast().onBackpressureBuffer();
    public final Sinks.Many<PremiumMessage> premiumSink = Sinks.many().multicast().onBackpressureBuffer();
    public final Sinks.Many<PremiumDetailMessage> premiumDetailSink = Sinks.many().multicast().onBackpressureBuffer();
}
