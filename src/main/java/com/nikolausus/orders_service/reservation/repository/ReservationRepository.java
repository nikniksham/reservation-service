package com.nikolausus.orders_service.reservation.repository;

import com.nikolausus.orders_service.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

}
