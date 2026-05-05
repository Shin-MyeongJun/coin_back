SELECT DISTINCT ON (ti.market_code_id)
    ti.id,
    ti.market_code_id,
    NULL            AS symbol,
    NULL            AS base_exchange_id,
    NULL            AS compare_exchange_id,
    ti.interval,
    ti.type,
    ti.period,
    ti.value,
    ti.bucket_open_ts,
    ti.bucket_close_ts,
    ti.observe_open_ts,
    ti.observe_close_ts
FROM tick_indicator ti
WHERE ti.market_code_id = ANY(:marketCodeIds)
  AND ti.interval        = :interval
  AND ti.type            = :type
ORDER BY ti.market_code_id, ti.bucket_close_ts DESC, ti.id DESC
