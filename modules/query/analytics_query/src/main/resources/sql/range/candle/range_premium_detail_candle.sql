SELECT  pcd.id,
        pcd.symbol
        pcd.base_exchange_id,
        pcd.compare_exchange_id,
        pcd.interval,
        pcd.open_base_price,
        pcd.open_base_quote_val
        pcd.open_compare_price,
        pcd.open_compare_quote_val
        pcd.high_base_price,
        pcd.high_base_quote_val
        pcd.high_compare_price,
        pcd.high_compare_quote_val
        pcd.low_base_price,
        pcd.low_base_quote_val
        pcd.low_compare_price,
        pcd.low_compare_quote_val
        pcd.close_base_price,
        pcd.close_base_quote_val
        pcd.close_compare_price,
        pcd.close_compare_quote_val
        pcd.bucket_open_ts,
        pcd.bucket_close_ts
FROM premium_detail_candle pcd
WHERE pcd.symbol = :symbol
  AND pcd.base_exchange_id = :baseExchangeId
  AND pcd.compare_exchange_id = :compareExchangeId
  AND pcd.interval = :interval
  AND pcd.bucket_open_ts BETWEEN :fromTs AND :toTs
