package com.example.demo.analytics_query.application.usecase;

import com.example.demo.analytics_query.application.dto.PremiumDetailCandleView;
import com.example.demo.analytics_query.application.port.out.GetPremiumDetailCandleForRecomputePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetPremiumDetailCandleForRecomputeUseCase {

    private final GetPremiumDetailCandleForRecomputePort port;

    public List<PremiumDetailCandleView> execute(String symbol, Long baseExchangeId, Long compareExchangeId,
                                                  String interval, long displayFromTs, long toTs, int lookbackBuckets) {
        long actualFromTs = displayFromTs - (long) lookbackBuckets * intervalToMillis(interval);
        return port.findSeriesWithLookback(symbol, baseExchangeId, compareExchangeId, interval, actualFromTs, toTs);
    }

    private static long intervalToMillis(String interval) {
        return switch (interval) {
            case "1m"  -> 60_000L;
            case "3m"  -> 180_000L;
            case "5m"  -> 300_000L;
            case "15m" -> 900_000L;
            case "30m" -> 1_800_000L;
            case "1h"  -> 3_600_000L;
            case "4h"  -> 14_400_000L;
            case "1d"  -> 86_400_000L;
            case "1w"  -> 604_800_000L;
            case "1M"  -> 2_592_000_000L;
            default    -> throw new IllegalArgumentException("Unsupported interval: " + interval);
        };
    }
}
