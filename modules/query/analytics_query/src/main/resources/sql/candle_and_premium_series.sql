SELECT
    tc.market_code_id,
    tc.interval,
    tc.open   AS candle_open,
    tc.high   AS candle_high,
    tc.low    AS candle_low,
    tc.close  AS candle_close,
    pc.open   AS premium_open,
    pc.high   AS premium_high,
    pc.low    AS premium_low,
    pc.close  AS premium_close,
    tc.bucket_open_ts,
    tc.bucket_close_ts
FROM tick_candle tc
JOIN premium_candle pc
    ON  tc.bucket_open_ts    = pc.bucket_open_ts
    AND tc.interval          = pc.interval
WHERE tc.market_code_id      = :marketCodeId
  AND pc.base_exchange_id    = :baseExchangeId
  AND pc.compare_exchange_id = :compareExchangeId
  AND tc.interval            = :interval
  AND tc.bucket_open_ts BETWEEN :fromTs AND :toTs
ORDER BY tc.bucket_open_ts
