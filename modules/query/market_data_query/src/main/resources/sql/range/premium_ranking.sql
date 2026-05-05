WITH latest AS (
    SELECT DISTINCT ON (symbol, base_exchange_id, compare_exchange_id)
        symbol,
        base_exchange_id,
        compare_exchange_id,
        bid
    FROM premium
    ORDER BY symbol, base_exchange_id, compare_exchange_id, timestamp DESC, id DESC
),
ranked_positive AS (
    SELECT
        symbol,
        base_exchange_id,
        compare_exchange_id,
        bid,
        ROW_NUMBER() OVER (ORDER BY bid DESC)::int AS rank
    FROM latest
    WHERE bid > 0
    LIMIT :limit
),
ranked_negative AS (
    SELECT
        symbol,
        base_exchange_id,
        compare_exchange_id,
        bid,
        ROW_NUMBER() OVER (ORDER BY bid ASC)::int AS rank
    FROM latest
    WHERE bid < 0
    LIMIT :limit
)
SELECT * FROM ranked_positive
UNION ALL
SELECT * FROM ranked_negative
ORDER BY bid DESC
