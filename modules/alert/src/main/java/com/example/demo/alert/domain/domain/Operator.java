package com.example.demo.alert.domain.domain;

import java.math.BigDecimal;

public enum Operator {
    GT(">") {
        @Override
        public boolean matches(BigDecimal observedValue, BigDecimal threshold) {
            return observedValue.compareTo(threshold) > 0;
        }
    },
    GTE(">=") {
        @Override
        public boolean matches(BigDecimal observedValue, BigDecimal threshold) {
            return observedValue.compareTo(threshold) >= 0;
        }
    },
    LT("<") {
        @Override
        public boolean matches(BigDecimal observedValue, BigDecimal threshold) {
            return observedValue.compareTo(threshold) < 0;
        }
    },
    LTE("<=") {
        @Override
        public boolean matches(BigDecimal observedValue, BigDecimal threshold) {
            return observedValue.compareTo(threshold) <= 0;
        }
    },
    EQ("=") {
        @Override
        public boolean matches(BigDecimal observedValue, BigDecimal threshold) {
            return observedValue.compareTo(threshold) == 0;
        }
    };

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public abstract boolean matches(BigDecimal observedValue, BigDecimal threshold);
}
