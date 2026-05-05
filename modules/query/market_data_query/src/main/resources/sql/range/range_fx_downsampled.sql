SELECT
    NULL::bigint                                                            AS id,
    base_currency,
    quote_currency,
    avg(rate)                                                               AS rate,
    extract(epoch FROM time_bucket(
        make_interval(secs => :bucketSeconds),
        to_timestamp(timestamp / 1000.0)
    ))::bigint * 1000                                                       AS timestamp
FROM fx
WHERE base_currency  = :baseCurrency
  AND quote_currency = :quoteCurrency
  AND timestamp BETWEEN :fromTs AND :toTs
GROUP BY base_currency, quote_currency,
         time_bucket(make_interval(secs => :bucketSeconds), to_timestamp(timestamp / 1000.0))
ORDER BY timestamp
