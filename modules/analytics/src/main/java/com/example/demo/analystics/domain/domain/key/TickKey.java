package com.example.demo.analystics.domain.domain.key;

public record TickKey (
    Long MarketCodeId
) implements DataKey<TickKey> {

    @Override
    public boolean equals(TickKey tickKey) {
        return tickKey.MarketCodeId.equals(MarketCodeId);
    }
}
