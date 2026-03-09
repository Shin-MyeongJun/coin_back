package com.example.demo.analystics.domain.service;

import com.example.demo.analystics.domain.domain.Interval;
import com.example.demo.analystics.domain.domain.TimeWindow;
import org.springframework.stereotype.Component;

@Component
public class BucketTimeAdjustService{

    public TimeWindow adjust(Long open, Long close, Interval interval) {

        long millis = interval.getMinutes() * 60_000L;
        long effectiveClose = close - 1;

        long bucketClose = (effectiveClose / millis) * millis + millis; // 구간의 끝
        long bucketOpen  = bucketClose - millis;

        return new TimeWindow(
                bucketOpen,
                bucketClose,
                open,
                close
        );
    }
}
