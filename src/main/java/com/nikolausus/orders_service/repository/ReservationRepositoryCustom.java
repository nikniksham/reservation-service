package com.nikolausus.orders_service.repository;


import java.time.LocalDateTime;

public interface ReservationRepositoryCustom {

    long markExpiredReservationAndGetQuantitySum(Long productId, LocalDateTime now);

}
