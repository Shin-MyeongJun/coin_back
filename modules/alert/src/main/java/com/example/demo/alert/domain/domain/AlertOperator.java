package com.example.demo.alert.domain.domain;

import java.math.BigDecimal;

public enum AlertOperator {
    GREATER_THAN {
        @Override
        public boolean apply(BigDecimal observed, BigDecimal threshold) {
            return observed.compareTo(threshold) > 0;
        }
    },
    GREATER_THAN_OR_EQUAL {
        @Override
        public boolean apply(BigDecimal observed, BigDecimal threshold) {
            return observed.compareTo(threshold) >= 0;
        }
    },
    LESS_THAN {
        @Override
        public boolean apply(BigDecimal observed, BigDecimal threshold) {
            return observed.compareTo(threshold) < 0;
        }
    },
    LESS_THAN_OR_EQUAL {
        @Override
        public boolean apply(BigDecimal observed, BigDecimal threshold) {
            return observed.compareTo(threshold) <= 0;
        }
    },
    CROSSES_ABOVE {
        @Override
        public boolean apply(BigDecimal observed, BigDecimal threshold) {
            throw new IllegalArgumentException("CROSSES_ABOVE is not supported by AlertEvaluator");
        }
    },
    CROSSES_BELOW {
        @Override
        public boolean apply(BigDecimal observed, BigDecimal threshold) {
            throw new IllegalArgumentException("CROSSES_BELOW is not supported by AlertEvaluator");
        }
    };

    public abstract boolean apply(BigDecimal observed, BigDecimal threshold);

    public static AlertOperator fromDomain(Operator operator) {
        return switch (operator) {
            case GT -> GREATER_THAN;
            case GTE -> GREATER_THAN_OR_EQUAL;
            case LT -> LESS_THAN;
            case LTE -> LESS_THAN_OR_EQUAL;
            case EQ -> throw new IllegalArgumentException("Operator EQ is not supported by AlertEvaluator");
        };
    }
}
