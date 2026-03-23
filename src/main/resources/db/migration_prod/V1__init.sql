CREATE TABLE product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    stock BIGINT NOT NULL,
    version BIGINT NOT NULL
);

CREATE TABLE reservation (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_product FOREIGN KEY(product_id) REFERENCES product(id)
);

-- индексы для ускорения поиска по резервам

-- индекс для составления ТОП-5 подтвержденных резерваций, product_id делает индекс covering (нет обращений в heap)
-- дополнительно указываем условие, чтобы индекс покрывал только необходимую часть строк
CREATE INDEX idx_reservation_status_createdAt_productId
    ON reservation(created_at, product_id)
WHERE status = 'CONFIRMED';

-- индекс для поиска всех истекших резерваций, также partial индекс по статусу, сортируем по продукту и дате истечения
-- product_id нужен для оптимизации optimistic locking. Тк система подразумевается высоконагруженной с кучей продуктов,
-- то запрос на очистку всех резерваций по всем продуктам одним махом будет очень трудновыполнимый
-- и постоянно ловящий OptimisticLockException (тк резервации меняют кол-во доступного товара в Product)
CREATE INDEX idx_reservation_status_productId_expiresAt
    ON reservation(product_id, expires_at)
WHERE status = 'ACTIVE';