package com.example.demo.analystics.domain.domain;

public record TimeWindow(
        Long bucketOpenTs,
        Long bucketCloseTs,
        Long observeOpenTs,
        Long observeCloseTs
) {
    private static final long ALLOWABLE_DRIFT_MS = 5_000L;


    public boolean isAcceptableDrift() {
        return isWithin(bucketOpenTs, observeOpenTs)
                && isWithin(bucketCloseTs, observeCloseTs);
    }

    public boolean isOpenDrifted() {
        return !isWithin(bucketOpenTs, observeOpenTs);
    }

    public boolean isCloseDrifted() {
        return !isWithin(bucketCloseTs, observeCloseTs);
    }

    private boolean isWithin(Long bucketTs, Long observeTs) {
        if (bucketTs == null || observeTs == null) {
            return false;
        }
        return Math.abs(bucketTs - observeTs) <= ALLOWABLE_DRIFT_MS;
    }
}
