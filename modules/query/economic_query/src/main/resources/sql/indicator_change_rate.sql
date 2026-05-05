SELECT
    e.observation_date,
    e.value,
    LAG(e.value) OVER (ORDER BY e.observation_date) AS previous_value,
    CASE
        WHEN LAG(e.value) OVER (ORDER BY e.observation_date) IS NULL THEN NULL
        WHEN LAG(e.value) OVER (ORDER BY e.observation_date) = 0    THEN NULL
        ELSE (e.value - LAG(e.value) OVER (ORDER BY e.observation_date))
             / LAG(e.value) OVER (ORDER BY e.observation_date)
    END AS change_rate
FROM economic_indicator e
WHERE e.ind_code_id = :indCodeId
ORDER BY e.observation_date
