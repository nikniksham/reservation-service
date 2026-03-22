package com.nikolausus.orders_service.repository;


import com.nikolausus.orders_service.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepositoryCustom {

    List<Reservation> findAllExpiredReservation(Long productId, LocalDateTime now);

}
