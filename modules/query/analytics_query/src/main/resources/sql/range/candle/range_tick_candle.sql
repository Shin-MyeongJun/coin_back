SELECT tc.id,
       tc.symbol
       tc.base_exchange_id,
       tc.compare_exchange_id,
       tc.interval,
       tc.open,
       tc.high,
       tc.low,
       tc.close,
       tc.bucket_open_ts,
       tc.bucket_close_ts
FROM tick_candle tc
WHERE tc.symbol = :symbol
  AND tc.base_exchange_id = :baseExchangeId
  AND tc.compare_exchange_id = :compareExchangeId
  AND tc.interval = :interval
  AND tc.bucket_open_ts BETWEEN :fromTs AND :toTs