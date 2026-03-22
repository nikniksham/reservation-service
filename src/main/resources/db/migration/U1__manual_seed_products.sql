-- Ручная миграция: генерация тестовых данных

-- создаём 20 продуктов
INSERT INTO product (name, stock, version)
SELECT 'Product - ' || i, (random() * 100 + 1)::int, 1
FROM generate_series(1,20) AS s(i);

-- генерируем 1000 резерваций
WITH reservation_data AS (
    SELECT
        (random()*20 + 1)::int AS product_id,
            (random()*10 + 1)::int AS quantity,
            CASE WHEN random() < 0.5 THEN 'ACTIVE' ELSE 'CONFIRMED' END AS status,
        now() - (random() * interval '24 hours') AS created_at
    FROM generate_series(1,1000)
)
INSERT INTO reservation (product_id, quantity, status, created_at, expires_at)
SELECT
    product_id,
    quantity,
    status,
    created_at,
    created_at + interval '10 minutes' AS expires_at
FROM reservation_data;