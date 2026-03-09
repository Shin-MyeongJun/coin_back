package com.example.demo.meta_data.domain;

public enum ExchangeType {
    SPOT("SPOT",ExchangeTypeGroup.SPOT),
    PERPETUAL("PERPETUAL",ExchangeTypeGroup.PERPETUAL),
    DELIVERY("DELIVERY",ExchangeTypeGroup.DELIVERY),
    CURRENT_QUARTER("CURRENT_QUARTER",ExchangeTypeGroup.DELIVERY),
    NEXT_QUARTER("NEXT_QUARTER",ExchangeTypeGroup.DELIVERY),
    OTHER("OTHER",ExchangeTypeGroup.OTHER);

    private final String name;
    private final ExchangeTypeGroup group;
    ExchangeType (String name,ExchangeTypeGroup group) {
        this.name = name;
        this.group = group;
    }
    public ExchangeTypeGroup getGroup() {
        return group;
    }
    public String getName() {
        return name;
    }

    public boolean isSameGroup(ExchangeType  other) {
        return this.group == other.group;
    }

    public static ExchangeType fromValue(String value) {
        for (ExchangeType  exchangeType : values()) {
            if (exchangeType.getName().equalsIgnoreCase(value)) {
                return exchangeType;
            }
        }
        return OTHER;
    }

}
