-- Ручная миграция: очистка таблиц

-- очищаем таблицы
TRUNCATE TABLE reservation CASCADE;
TRUNCATE TABLE product CASCADE;

-- перестраиваем индексы
REINDEX TABLE reservation;
REINDEX TABLE product;