# Spring + PostgreSQL + Flyway

### Краткое представление
Проект представляет собой распределенный сервис резервирования товаров со следующими правилами:
1) Есть `product`, каждый из которых имеет некоторый stock (количество на складе)
2) Можно создавать `reservation` продукта (`POST /reservations?productId=XXX&quantity=YYY`):
   - `reservation.quantity` должен быть <= `product.stock`
   - `reservation` создается на 10 минут
   - Каждый созданный `reservation` уменьшает `product.stock` на `reservation.quantity`
   - Если за отведенные 10 минут не подтвердить резервацию (`POST /reservations/{id}/confirm`), то она отмениться автоматически, `product.stock` увеличится на `reservation.quantity`
   - Если резервацию подтвердить, то она заимеет статус `CONFIRMED` и пойдет в зачет статистики продукта
   - Также резервацию можно отменить (`POST /reservations/{id}/cancel`)
3) Ручка `GET /products/top-reserved` возвращает топ-5 продуктов, по сумме `reservation.quantity` за последние сутки со статусом `CONFIRMED` 
4) Ручка `GET /products/{id}` возвращает информацию о продукте


### Блокировки
Подразумевается, что сервисом будет пользоваться много юзеров, все из которых могут пытаться разом изменить `product.stock` одного товара, по-этому, для избежания проблем, была использована OptimisticLocking и транзакции с Retry 