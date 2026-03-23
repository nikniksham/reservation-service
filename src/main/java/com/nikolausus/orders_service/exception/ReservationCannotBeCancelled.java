package com.nikolausus.orders_service.exception;

public class ReservationCannotBeCancelled extends RuntimeException {
    public ReservationCannotBeCancelled(Long reservationId) {
        super("Reservation cannot be confirmed, id: " + reservationId);
    }
}
