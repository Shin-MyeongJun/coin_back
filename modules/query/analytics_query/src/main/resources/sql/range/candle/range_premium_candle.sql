SELECT pc.id,
       pc.symbol
       pc.base_exchange_id,
       pc.compare_exchange_id,
       pc.interval,
       pc.open,
       pc.high,
       pc.low,
       pc.close,
       pc.bucket_open_ts,
       pc.bucket_close_ts
FROM premium_candle pc
WHERE pc.symbol = :symbol
 AND pc.base_exchange_id = :baseExchangeId
 AND pc.compare_exchange_id = :compareExchangeId
 AND pc.interval = :interval
 AND pc.bucket_open_ts BETWEEN :fromTs AND :toTs

