SELECT
    pd.id,
    pd.symbol,
    pd.base_exchange_id,
    pd.compare_exchange_id,
    pd."timestamp",
    pd.base_bid,
    pd.base_ask,
    pd.base_quote,
    pd.compare_bid,
    pd.compare_ask,
    pd.compare_quote
FROM premium_detail pd
WHERE pd.base_exchange_id = :baseExchangeId
  AND pd.compare_exchange_id = :compareExchangeId
  AND pd.symbol =:target
  AND pd.timestamp BETWEEN :fromTs AND :toTs
ORDER BY pd."timestamp" DESC;