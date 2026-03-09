package com.example.demo.analystics.domain.domain;

public enum Interval {
    M1   (1,    "1m"),
    M3   (3,    "3m"),
    M5   (5,    "5m"),
    M15  (15,   "15m"),
    M30  (30,   "30m"),
    M60  (60,   "1h"),
    M240 (240,  "4h"),
    D1   (1440, "1d"),
    W1   (10080,"1w"),
    MN1  (43200,"1M");

    private final int minutes;
    private final String period;   // 외부에서 주로 쓰는 문자열 표현

    Interval(int minutes, String period) {
        this.minutes = minutes;
        this.period  = period;
    }

    public int getMinutes() {
        return minutes;
    }
    public int getSecond(){return minutes*60;}
    public String getPeriod() {return period;}

    public static Interval fromMinutes(int minutes) {
        for (Interval interval : values()) {
            if (interval.minutes == minutes) {
                return interval;
            }
        }
        throw new IllegalArgumentException("Unsupported interval: " + minutes);
    }

    public static Interval fromPeriod(String period) {
        for (Interval ci : values()) {
            if (ci.period.equalsIgnoreCase(period)) return ci;
        }
        throw new IllegalArgumentException("Unsupported period: " + period);
    }
}
