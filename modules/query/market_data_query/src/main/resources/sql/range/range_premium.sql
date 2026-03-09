SELECT
    p.id,
    p.symbol,
    p.base_exchange_id,
    p.compare_exchange_id,
    p."timestamp",
    p.bid,
    p.ask
FROM premium p
WHERE p.base_exchange_id = :baseExchangeId
  AND p.compare_exchange_id = :compareExchangeId
  AND p.symbol =:target
  AND p.timestamp BETWEEN :fromTs AND :toTs
ORDER BY p."timestamp" DESC,