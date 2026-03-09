package com.example.demo.analystics.domain.domain.key;

public record PremiumKey(
  String base,
  Long baseExchangeId,
  Long compareExchangeId
) implements DataKey<PremiumKey> {
    @Override
    public boolean equals(PremiumKey premiumKey) {
        return premiumKey.base.equals(base) && premiumKey.baseExchangeId.equals(baseExchangeId) && premiumKey.compareExchangeId.equals(compareExchangeId);
    }
}
